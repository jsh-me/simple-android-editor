package kr.co.domain.koin.repositoryimpl

import kr.co.domain.koin.repository.HttpClientRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class HttpClientRepositoryImpl  : HttpClientRepository {
    override fun getOkHttp(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.readTimeout(1, TimeUnit.MINUTES)
        httpClient.connectTimeout(30, TimeUnit.SECONDS)
        httpClient.addInterceptor {  chain: Interceptor.Chain ->
            var request = chain.request() //Log
            request = request.newBuilder()
                .method(request.method(), request.body())
                .build()
            chain.proceed(request) //Log
        }

        return httpClient.build()
    }
}