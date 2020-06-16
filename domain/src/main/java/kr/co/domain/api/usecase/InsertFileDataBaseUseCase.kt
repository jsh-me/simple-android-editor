package kr.co.domain.api.usecase

import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.koin.repository.room.ResultFileStorageRepository

class InsertFileDataBaseUseCase(private val resultFileStorageRepository: ResultFileStorageRepository){

    fun insert(fileStorage: ResultFileStorage) = resultFileStorageRepository.insert(fileStorage)
}