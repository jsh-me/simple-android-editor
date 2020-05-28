package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.ImagePidDataList

data class ImagePidNumberResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: ImagePidDataList,
    @JsonProperty("message") var message: String
)