package kr.co.domain.api.usecase

import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.koin.repository.room.ResultFileStorageRepository

class InsertFileDataBaseUseCase(resultFileStorageRepository: ResultFileStorageRepository){
    private val resultFileStorageRepository = resultFileStorageRepository

    fun insert(fileStorage: ResultFileStorage) = resultFileStorageRepository.insert(fileStorage)
}