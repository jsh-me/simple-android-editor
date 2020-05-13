package kr.co.domain.api.service

import io.reactivex.Single
import kr.co.data.request.UserRequest
import kr.co.data.response.UserResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {
    @FormUrlEncoded
    @POST("login/login.do")
    fun postLogin(@Field("user") user: UserRequest) : Single<UserResponse>
}