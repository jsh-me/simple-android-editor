package kr.co.domain.koin.modules

import androidx.room.Room
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.api.room.AppDatabase
import kr.co.domain.api.usecase.*
import kr.co.domain.koin.repository.room.ResultFileStorageRepository
import kr.co.domain.koin.repositoryimpl.room.ResultFileStorageRepositoryImpl
import org.koin.dsl.module

val databaseModule = module {
    single<ResultFileStorageRepository> { ResultFileStorageRepositoryImpl(get()) }

    single { Room.databaseBuilder(get(), AppDatabase::class.java, "database").fallbackToDestructiveMigration().build()}

    single { get<AppDatabase>().resultFileStorageDao()}

    //----Room UseCase ----//
    factory { AllDeleteFileDataBaseUseCase(get()) }

    factory { AllLoadFileDataBaseUseCase(get()) }

    factory { InsertFileDataBaseUseCase(get()) }

}