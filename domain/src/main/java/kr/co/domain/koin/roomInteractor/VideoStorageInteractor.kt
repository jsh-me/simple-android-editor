package kr.co.domain.koin.roomInteractor

import io.reactivex.Observable
import kr.co.data.entity.room.VideoStorage

interface VideoStorageInteractor {

    fun update(videoStorage: VideoStorage)

    fun delete(videoStorage: VideoStorage)

    fun deleteAllVideoStorage()

    fun getAllVideoStorage() : Observable<List<VideoStorage>>
}