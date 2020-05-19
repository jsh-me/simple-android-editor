package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

data class FileDownloadResponse(
    @JsonProperty("url") var url: URL
)