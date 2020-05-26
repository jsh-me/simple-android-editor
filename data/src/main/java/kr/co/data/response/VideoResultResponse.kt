package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.local.VideoResultList

data class VideoResultResponse (
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: VideoResultList,
    @JsonProperty("message") var message: String
)