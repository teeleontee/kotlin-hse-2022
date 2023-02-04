package homework03.topics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChildData(
    @JsonProperty("name") val author: String,
    @JsonProperty("title") val header: String,
    @JsonProperty("selftext") val selfText: String?,
    @JsonProperty("selftext_html") val selfTextHtml: String?,
    @JsonProperty("created") val timeOfPublication: Long,
    @JsonProperty("id") val publicationId: String,
    @JsonProperty("permalink") val permaLink: String,
    @JsonProperty("ups") val upvotes: Int,
    @JsonProperty("downs") val downvotes: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TopicDiscussion(@JsonProperty("data") val data: TopicDiscussionData) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class TopicDiscussionData(@JsonProperty("children") val children: List<Child>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        internal data class Child(@JsonProperty("data") val data: ChildData)
    }
}

