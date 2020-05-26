package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoResultList(
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("createdDate") var createdDate : Long,
    @JsonProperty("reqEditType") var reqEditType: String,
    @JsonProperty("title") var title: String,
    @JsonProperty("videoFile") var videoFile: FileInfo,
    @JsonProperty("maskImg") var maskImg: MaskImgInfo,
    @JsonProperty("resultFile") var resultFile: ResultInfo?
)