package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class MaskImgInfo(
    @JsonProperty("objectPid") var objectPid: String
)