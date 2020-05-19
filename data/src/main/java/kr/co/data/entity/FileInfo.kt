package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class FileInfo(
    @JsonProperty("objectPid") var objectPid: String
)