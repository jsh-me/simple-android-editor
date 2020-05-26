package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.local.AllImageResultList
import kr.co.data.entity.local.AllVideoResultList

data class AllImageResultResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: AllImageResultList,
    @JsonProperty("message") var message: String
)