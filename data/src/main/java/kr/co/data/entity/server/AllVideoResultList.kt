package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class AllVideoResultList (
    @JsonProperty("list") var list: ArrayList<VideoResultList>
)