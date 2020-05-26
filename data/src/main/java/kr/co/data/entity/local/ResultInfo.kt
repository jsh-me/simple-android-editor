package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class ResultInfo(
    @JsonProperty("objectPid") var objectPid: String,
    @JsonProperty("fileName") var fileId: String
)