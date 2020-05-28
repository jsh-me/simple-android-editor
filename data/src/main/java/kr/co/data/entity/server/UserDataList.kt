package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDataList(
    @JsonProperty("user") var user: UserInfo
)