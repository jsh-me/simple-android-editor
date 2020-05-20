package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.VideoPidDataList

data class VideoPidNumberResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: VideoPidDataList,
    @JsonProperty("message") var message: String
)