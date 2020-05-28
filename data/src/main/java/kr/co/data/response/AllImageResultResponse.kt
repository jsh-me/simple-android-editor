package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.AllImageResultList

data class AllImageResultResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: AllImageResultList,
    @JsonProperty("message") var message: String
)