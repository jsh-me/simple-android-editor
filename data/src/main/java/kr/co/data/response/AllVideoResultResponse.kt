package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.local.AllVideoResultList
import kr.co.data.entity.local.FileDataList

data class AllVideoResultResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: AllVideoResultList,
    @JsonProperty("message") var message: String
)