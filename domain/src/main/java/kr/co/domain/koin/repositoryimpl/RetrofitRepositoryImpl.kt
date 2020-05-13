package kr.co.domain.koin.repositoryimpl

import kr.co.domain.globalconst.UrlConst.BASEURL
import kr.co.domain.koin.repository.HttpClientRepository
import kr.co.domain.koin.repository.RetrofitRepository
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

class RetrofitRepositoryImpl(private val okHttpRepo :HttpClientRepository ) : RetrofitRepository {
    override fun getRetrofit(): Retrofit {
        val client = okHttpRepo.getOkHttp()
        val baseUrl = URL(BASEURL)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
    }
}