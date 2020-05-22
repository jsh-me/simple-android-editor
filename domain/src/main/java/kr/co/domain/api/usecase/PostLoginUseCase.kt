package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.UserResponse
import kr.co.domain.api.service.LoginService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class PostLoginUseCase(retrofitRepository: RetrofitRepository) {
    private val loginService = retrofitRepository
        .getRetrofit()
        .create(LoginService::class.java)

    fun postLogin(id: String, passwd: String) : Single<UserResponse> = loginService
        .postLogin(id,passwd)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
