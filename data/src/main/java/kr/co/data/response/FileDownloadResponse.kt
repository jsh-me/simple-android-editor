package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File
import java.net.URL

data class FileDownloadResponse(
    @JsonProperty("file") var file: File
)