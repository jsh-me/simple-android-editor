package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class FileDataList(
    @JsonProperty("fileName") var fileName: String,
    @JsonProperty("fileType") var fileType: String
)