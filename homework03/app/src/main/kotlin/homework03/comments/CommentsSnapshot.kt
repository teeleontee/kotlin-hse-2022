package homework03.comments

data class CommentsSnapshot(
    val postId: String,
    val comments: List<Comment>,
    val created: Long = System.currentTimeMillis()
) {
    data class Comment(
        val id: String,
        val author: String?,
        val created: Long,
        val upvotes: Int,
        val downvotes: Int,
        val text: String?,
        val replTo: String,
        val depthInt: Int
    )
}
