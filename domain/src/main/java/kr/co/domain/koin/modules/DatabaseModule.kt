package kr.co.domain.koin.modules

import androidx.room.Room
import kr.co.domain.api.room.AppDatabase
import kr.co.domain.koin.repository.room.VideoStorageRepository
import kr.co.domain.koin.repositoryimpl.room.VideoStorageRepositoryImpl
import kr.co.domain.koin.roomInteractor.VideoStorageInteractor
import kr.co.domain.koin.roomInteractor.VideoStorageInteractorImpl
import org.koin.dsl.module

val databaseModule = module {
    single<VideoStorageRepository> { VideoStorageRepositoryImpl(get())}

    single<VideoStorageInteractor> { VideoStorageInteractorImpl(get())}

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").build()}

    single { get<AppDatabase>().videoStorageDao()}

}