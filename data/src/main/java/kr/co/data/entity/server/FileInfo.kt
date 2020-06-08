package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class FileInfo(
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("fileType") var fileType: String
)