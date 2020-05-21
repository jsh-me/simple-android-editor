package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.local.FileDataList

data class FileUploadResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: FileDataList,
    @JsonProperty("message") var message: String
)