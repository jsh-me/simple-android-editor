package kr.co.domain.koin.modules

import kr.co.domain.koin.repository.HttpClientRepository
import kr.co.domain.koin.repository.RetrofitRepository
import kr.co.domain.koin.repositoryimpl.HttpClientRepositoryImpl
import kr.co.domain.koin.repositoryimpl.RetrofitRepositoryImpl
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single<RetrofitRepository> { RetrofitRepositoryImpl(get())}
    single<HttpClientRepository> { HttpClientRepositoryImpl() }
}