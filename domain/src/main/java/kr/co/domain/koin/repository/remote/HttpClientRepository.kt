package kr.co.domain.koin.repository.remote

import okhttp3.OkHttpClient

interface HttpClientRepository {
    fun getOkHttp() : OkHttpClient
}