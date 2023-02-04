package homework03

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.korio.async.launch
import homework03.comments.CommentsSnapshot.Comment
import homework03.comments.CommentInfo
import homework03.topics.*
import homework03.comments.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException

sealed class ClientExceptions(msg: String) : RuntimeException(msg) {
    class ClientConnectionException(name: String) : ClientExceptions("Can't connect to $name")
    class NoSuchNodeException(name: String) : ClientExceptions("No such node : $name")
}

class RedditClient {
    private val client = HttpClient(CIO)
    private val objMapper = ObjectMapper()

    companion object {
        private const val redditDomain = "https://www.reddit.com"
        internal fun jsonSubRedditUrl(subReddit: String) = "$redditDomain/r/$subReddit/.json"
        internal fun jsonAboutSubRedditUrl(subReddit: String) = "$redditDomain/r/$subReddit/about.json"
        internal fun jsonIdUrl(id: String) = "$redditDomain/$id/.json"
    }

    private suspend fun getBodyString(url: String) =
        try {
            client.get(url).body<String>()
        } catch (e: RuntimeException) {
            throw ClientExceptions.ClientConnectionException(url)
        }

    private suspend fun getTopic(name: String): TopicSnapshot = coroutineScope {
        val getTopicInfo = async {
            val topicInfoString = getBodyString(jsonAboutSubRedditUrl(name))
            objMapper.readValue(topicInfoString, TopicInfo::class.java).data
        }

        val getTopicDiscussions = async {
            val topicDiscussionString = getBodyString(jsonSubRedditUrl(name))
            objMapper.readValue(topicDiscussionString, TopicDiscussion::class.java)
        }

        val info = getTopicInfo.await()
        val discussions = getTopicDiscussions.await()

        TopicSnapshot(
            info.dateCreated,
            info.cntFollowersOnline,
            info.topicDescription,
            discussions.data.children.map { it.data }
        )
    }

    private suspend fun getComments(title: String): CommentsSnapshot = coroutineScope {
        val comments = ArrayList<Comment>()

        fun recursiveGetComments(
            node: JsonNode,
            repl: String = "",
            depth: Int = 0
        ) {
            val comment = objMapper.treeToValue(node, CommentInfo::class.java).data
            comments.add(
                Comment(
                    id = comment.id,
                    author = comment.author,
                    created = comment.created,
                    upvotes = comment.upvote,
                    downvotes = comment.downvote,
                    text = comment.text,
                    replTo = repl,
                    depthInt = depth
                )
            )
            val replyNode = node["data"]["replies"] ?: return
            if (replyNode.size() == 0) return

            val replies = replyNode["data"]["children"]
            for (reply in replies) {
                recursiveGetComments(reply, comments.last().id, depth + 1)
            }
        }
        val getTree = async {
            val commentInfoString = getBodyString(jsonIdUrl(title))
            objMapper.readTree(commentInfoString)
        }

        val tree = getTree.await()
        val children = tree[1]["data"]["children"]
            ?: throw ClientExceptions.NoSuchNodeException("no data -> children node")

        for (child in children) {
            recursiveGetComments(child)
        }

        CommentsSnapshot(
            postId = tree[0]["data"]["children"][0]["data"]["id"].asText(),
            comments = comments.toList()
        )
    }

    suspend fun parse(name: String) = coroutineScope {
        val discussion = getTopic(name).discussions
        val allComments = mutableListOf<List<Comment>>()
        for (comment in discussion) {
            allComments.add(getComments(comment.permaLink).comments)
        }
        launch {
            writeToCsv(data = discussion, klass = ChildData::class, file = "$name-subjects.csv")
        }
        launch {
            writeToCsv(data = allComments.flatten(), klass = Comment::class, file = "$name-comments.csv")
        }
    }
}

