package kr.co.data.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DataList(
    @JsonProperty("user") var user: ArrayList<UserInfo>
)