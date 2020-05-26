package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class AllVideoResultList (
    @JsonProperty("list") var list: ArrayList<VideoResultList>
)