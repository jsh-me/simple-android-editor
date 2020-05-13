package kr.co.domain.koin.repository

import okhttp3.OkHttpClient

interface HttpClientRepository {
    fun getOkHttp() : OkHttpClient
}