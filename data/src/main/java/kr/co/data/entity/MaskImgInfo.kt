package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class MaskImgInfo(
    @JsonProperty("objectPid") var objectPid: String
)