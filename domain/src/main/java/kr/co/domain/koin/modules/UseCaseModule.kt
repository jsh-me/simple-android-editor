package kr.co.domain.koin.modules

import kr.co.domain.api.usecase.PostLoginUseCase
import org.koin.dsl.module

val usecaseModule = module{
    factory { PostLoginUseCase(get()) }
}