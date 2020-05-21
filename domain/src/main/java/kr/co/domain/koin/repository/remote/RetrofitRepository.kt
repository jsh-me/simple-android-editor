package kr.co.domain.koin.repository.remote

import retrofit2.Retrofit

interface RetrofitRepository {
    fun getRetrofit() : Retrofit
}