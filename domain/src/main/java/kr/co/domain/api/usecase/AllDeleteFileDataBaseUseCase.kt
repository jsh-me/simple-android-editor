package kr.co.domain.api.usecase

import kr.co.domain.koin.repository.room.ResultFileStorageRepository

class AllDeleteFileDataBaseUseCase(resultFileStorageRepository: ResultFileStorageRepository){
    private val resultFileStorageRepository = resultFileStorageRepository

    fun allDelete() = resultFileStorageRepository.deleteAllStorage()
}