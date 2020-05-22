package kr.co.domain.koin.roomInteractor

import io.reactivex.Observable
import kr.co.data.entity.room.VideoStorage
import kr.co.domain.koin.repository.room.VideoStorageRepository

class VideoStorageInteractorImpl(private val videoStorageRepository: VideoStorageRepository) :
    VideoStorageInteractor {

    override fun update(videoStorage: VideoStorage) {
        videoStorageRepository.update(videoStorage)
    }

    override fun delete(videoStorage: VideoStorage) {
        videoStorageRepository.delete(videoStorage)
    }

    override fun deleteAllVideoStorage() {
        videoStorageRepository.deleteAllVideoStorage()
    }

    override fun getAllVideoStorage(): Observable<List<VideoStorage>> {
       return videoStorageRepository.getAllVideoStorage()
    }
}