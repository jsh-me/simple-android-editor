package kr.co.domain.api.usecase

import kr.co.domain.koin.repository.room.ResultFileStorageRepository

class AllDeleteFileDataBaseUseCase(private val resultFileStorageRepository: ResultFileStorageRepository){

    fun allDelete() = resultFileStorageRepository.deleteAllStorage()
}