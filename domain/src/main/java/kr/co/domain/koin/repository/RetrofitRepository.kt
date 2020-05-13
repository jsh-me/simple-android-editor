package kr.co.domain.koin.repository

import retrofit2.Retrofit

interface RetrofitRepository {
    fun getRetrofit() : Retrofit
}