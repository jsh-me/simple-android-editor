package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class FileDataList(
    @JsonProperty("fileName") var fileName: String,
    @JsonProperty("fileType") var fileType: String,
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("fileId") var fileId: Long
)