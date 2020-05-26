package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.local.VideoPidDataList
import kr.co.data.entity.local.VideoResultList

data class VideoPidNumberResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: VideoPidDataList,
    @JsonProperty("message") var message: String
)