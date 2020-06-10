package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.VideoResultList

data class SearchResultVideoResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: ArrayList<VideoResultList>,
    @JsonProperty("message") var message: String
)