package kr.co.domain.api.usecase
import kr.co.data.entity.room.VideoStorage
import kr.co.domain.koin.repository.room.VideoStorageRepository

class InsertVideoDataBaseUseCase(videoStorageRepository: VideoStorageRepository) {
    private val videoStorageStorage = videoStorageRepository

    fun insert(videoStorage: VideoStorage)  = videoStorageStorage.insert(videoStorage)
}
