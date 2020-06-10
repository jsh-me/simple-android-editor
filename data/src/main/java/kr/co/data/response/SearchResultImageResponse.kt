package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.ImageResultList

data class SearchResultImageResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: ArrayList<ImageResultList>,
    @JsonProperty("message") var message: String
)