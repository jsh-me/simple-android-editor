package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDataList(
    @JsonProperty("user") var user: UserInfo
)