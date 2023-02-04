package homework03.topics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// about.json
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TopicInfo(@JsonProperty("data") val data: TopicInfoData) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class TopicInfoData(
        @JsonProperty("created") val dateCreated: Long,
        @JsonProperty("subscribers") val cntFollowersOnline: Int,
        @JsonProperty("public_description") val topicDescription: String
    )
}
