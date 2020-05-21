package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDataList(
    @JsonProperty("user") var user: UserInfo
)