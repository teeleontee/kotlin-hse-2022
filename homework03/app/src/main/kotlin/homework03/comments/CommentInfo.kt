package homework03.comments

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class CommentInfo(@JsonProperty("data") val data: CommentInfoData) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class CommentInfoData(
        @JsonProperty("id") val id: String,
        @JsonProperty("author") val author: String?,
        @JsonProperty("created") val created: Long,
        @JsonProperty("ups") val upvote: Int,
        @JsonProperty("downs") val downvote: Int,
        @JsonProperty("body") val text: String?
    )
}

