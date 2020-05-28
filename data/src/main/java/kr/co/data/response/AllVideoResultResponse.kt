package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.AllVideoResultList

data class AllVideoResultResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: AllVideoResultList,
    @JsonProperty("message") var message: String
)