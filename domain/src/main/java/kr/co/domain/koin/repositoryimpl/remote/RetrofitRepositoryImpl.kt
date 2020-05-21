package kr.co.domain.koin.repositoryimpl.remote

import kr.co.domain.globalconst.UrlConst.BASEURL
import kr.co.domain.koin.repository.remote.HttpClientRepository
import kr.co.domain.koin.repository.remote.RetrofitRepository
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitRepositoryImpl(private val okHttpRepo : HttpClientRepository) :
    RetrofitRepository {
    override fun getRetrofit(): Retrofit {
        val client = okHttpRepo.getOkHttp()
        val baseUrl = BASEURL

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
    }
}