package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class ImageResultList(
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("createdDate") var createdDate : Long,
    @JsonProperty("reqEditType") var reqEditType: String,
    @JsonProperty("title") var title: String,
    @JsonProperty("targetImg") var targetImg: FileInfo,
    @JsonProperty("maskImg") var maskImg: MaskImgInfo,
    @JsonProperty("resultFile") var resultFile: ResultInfo?
)