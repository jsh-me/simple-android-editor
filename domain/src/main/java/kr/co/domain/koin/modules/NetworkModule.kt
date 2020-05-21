package kr.co.domain.koin.modules

import kr.co.domain.koin.repository.remote.HttpClientRepository
import kr.co.domain.koin.repository.remote.RetrofitRepository
import kr.co.domain.koin.repositoryimpl.remote.HttpClientRepositoryImpl
import kr.co.domain.koin.repositoryimpl.remote.RetrofitRepositoryImpl
import org.koin.dsl.module

val networkModule = module {
    single<RetrofitRepository> { RetrofitRepositoryImpl(get()) }
    single<HttpClientRepository> { HttpClientRepositoryImpl() }
}