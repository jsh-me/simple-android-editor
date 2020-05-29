package kr.co.domain.api.usecase


import kr.co.domain.koin.repository.room.VideoStorageRepository

class AllDeleteVideoDataBaseUseCase(videoStorageRepository: VideoStorageRepository) {
    private val videoStorageStorage = videoStorageRepository

    fun allDelete() = videoStorageStorage.deleteAllVideoStorage()

}