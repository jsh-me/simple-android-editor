package kr.co.data.entity.local

import com.fasterxml.jackson.annotation.JsonProperty

data class AllImageResultList(
    @JsonProperty("list") var list: ArrayList<ImageResultList>
)