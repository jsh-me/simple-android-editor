package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.DataList

data class UserResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: ArrayList<DataList>
)