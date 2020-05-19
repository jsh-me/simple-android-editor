package kr.co.domain.koin.modules

import kr.co.domain.api.usecase.GetFileDownloadUseCase
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.api.usecase.PostLoginUseCase
import org.koin.dsl.module

val useCaseModule = module{
    factory { PostLoginUseCase(get()) }
    factory { PostFileUploadUseCase(get())}
    factory { GetFileDownloadUseCase(get()) }
}