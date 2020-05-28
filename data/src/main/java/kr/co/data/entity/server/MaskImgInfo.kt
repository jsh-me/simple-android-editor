package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class MaskImgInfo(
    @JsonProperty("objectPid") var objectPid: String
)