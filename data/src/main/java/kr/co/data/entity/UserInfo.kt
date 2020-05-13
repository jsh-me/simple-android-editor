package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
    @JsonProperty("email") var email:String,
    @JsonProperty("userName") var userName:String
)