package homework03.topics

data class TopicSnapshot(
    val dateCreated: Long,
    val followersOnlineCount:Int,
    val description: String,
    val discussions: List<ChildData>,
    val snapshotCreated: Long = System.currentTimeMillis()
)
