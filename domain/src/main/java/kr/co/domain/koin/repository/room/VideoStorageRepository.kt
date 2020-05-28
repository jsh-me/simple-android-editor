package kr.co.domain.koin.repository.room

import io.reactivex.Observable
import kr.co.data.entity.room.VideoStorage

interface VideoStorageRepository {

    fun insert(videoStorage: VideoStorage)

    fun update(videoStorage: VideoStorage)

    fun delete(videoStorage: VideoStorage)

    fun deleteAllVideoStorage()

    fun getAllVideoStorage() : Observable<List<VideoStorage>>

}