package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class UserInfo(
    @JsonProperty("email") var email:String,
    @JsonProperty("userName") var userName:String
)