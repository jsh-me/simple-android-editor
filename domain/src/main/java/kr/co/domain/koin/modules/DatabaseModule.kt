package kr.co.domain.koin.modules

import androidx.room.Room
import kr.co.domain.api.room.AppDatabase
import kr.co.domain.api.usecase.*
import kr.co.domain.koin.repository.room.ImageStorageRepository
import kr.co.domain.koin.repository.room.VideoStorageRepository
import kr.co.domain.koin.repositoryimpl.room.ImageStorageRepositoryImpl
import kr.co.domain.koin.repositoryimpl.room.VideoStorageRepositoryImpl
import org.koin.dsl.module

val databaseModule = module {
    single<VideoStorageRepository> { VideoStorageRepositoryImpl(get()) }

    single<ImageStorageRepository> { ImageStorageRepositoryImpl(get()) }

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").fallbackToDestructiveMigration().build()}

    single { get<AppDatabase>().videoStorageDao() }

    single { get<AppDatabase>().imageStorageDao() }

    //----Room UseCase ----//
    factory { InsertVideoDataBaseUseCase(get()) }

    factory { InsertImageDataBaseUseCase(get()) }

    factory { AllLoadVideoDataBaseUseCase(get()) }

    factory { AllDeleteVideoDataBaseUseCase(get()) }

    factory { AllDeleteImageDataBaseUseCase(get()) }

    factory { AllLoadImageDataBaseUseCase(get()) }

}