package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class ImagePidDataList(
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("createdDate") var createdDate : Long,
    @JsonProperty("reqEditType") var reqEditType: String,
    @JsonProperty("targetImg") var targetImg: FileInfo,
    @JsonProperty("maskImg") var maskImg: MaskImgInfo,
    @JsonProperty("frameTimeSec") var frameTimeSec: Float
)