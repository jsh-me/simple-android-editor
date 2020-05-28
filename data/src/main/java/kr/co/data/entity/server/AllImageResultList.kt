package kr.co.data.entity.server

import com.fasterxml.jackson.annotation.JsonProperty

data class AllImageResultList(
    @JsonProperty("list") var list: ArrayList<ImageResultList>
)