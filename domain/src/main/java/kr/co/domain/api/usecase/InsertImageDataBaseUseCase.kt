package kr.co.domain.api.usecase

import kr.co.data.entity.room.ImageStorage
import kr.co.domain.koin.repository.room.ImageStorageRepository

class InsertImageDataBaseUseCase(imageStorageRepository: ImageStorageRepository) {
    private val imageStorageStorage = imageStorageRepository

    fun insert(imageStorage: ImageStorage)  = imageStorageStorage.insert(imageStorage)
}
