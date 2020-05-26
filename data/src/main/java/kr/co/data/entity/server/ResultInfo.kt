package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class ResultInfo(
    @JsonProperty("objectPid") var objectPid: String?,
    @JsonProperty("fileName") var fileId: String
)