package kr.co.domain.koin.repositoryimpl

import android.util.Log
import kr.co.domain.BuildConfig
import kr.co.domain.globalconst.CookieClass
import kr.co.domain.koin.repository.HttpClientRepository
import kr.co.domain.koin.repository.RetrofitRepository
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.HashSet
import java.util.concurrent.TimeUnit

class HttpClientRepositoryImpl : HttpClientRepository{

    override fun getOkHttp(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor {  chain: Interceptor.Chain ->
            var request = chain.request() //Log
            request = request.newBuilder()
                .method(request.method(), request.body())
                .build()
            chain.proceed(request) //Log

        }


        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(loggingInterceptor)
        }
        httpClient.readTimeout(1, TimeUnit.MINUTES)
        httpClient.connectTimeout(30, TimeUnit.SECONDS)

        httpClient.interceptors().add(ReceivedCookiesInterceptor())
        httpClient.interceptors().add(AddCookiesInterceptor())
        return httpClient.build()
    }
}