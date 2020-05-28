package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
    @JsonProperty("email") var email:String,
    @JsonProperty("userName") var userName:String
)