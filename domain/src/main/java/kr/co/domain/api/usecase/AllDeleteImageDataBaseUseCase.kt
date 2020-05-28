package kr.co.domain.api.usecase

import kr.co.domain.koin.repository.room.ImageStorageRepository
import kr.co.domain.koin.repository.room.VideoStorageRepository

class AllDeleteImageDataBaseUseCase(imageStorageRepository: ImageStorageRepository) {
    private val imageStorageStorage = imageStorageRepository

    fun allDelete() = imageStorageStorage.deleteAllImageStorage()

}