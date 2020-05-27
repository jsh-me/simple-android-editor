package kr.co.domain.api.usecase

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.VideoStorage
import kr.co.domain.koin.repository.room.VideoStorageRepository

class AllLoadVideoDataBaseUseCase(videoStorageRepository: VideoStorageRepository) {
    private val videoStorageStorage = videoStorageRepository

    fun allLoad(): Observable<List<VideoStorage>> =
        videoStorageStorage.getAllVideoStorage()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
