package kr.co.data.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRequest(
    @JsonProperty("email") var email: String,
    @JsonProperty("passwd") var passwd: String
)