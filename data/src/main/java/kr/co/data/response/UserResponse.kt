package kr.co.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.data.entity.server.UserDataList

data class UserResponse(
    @JsonProperty("status") var status:String,
    @JsonProperty("datas") var datas: UserDataList,
    @JsonProperty("message") var message: String
)