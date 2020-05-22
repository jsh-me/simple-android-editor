package kr.co.domain.koin.repositoryimpl.room

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.Observable
import kr.co.data.entity.room.VideoStorage
import kr.co.domain.api.room.VideoStorageDao
import kr.co.domain.koin.repository.room.VideoStorageRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class VideoStorageRepositoryImpl(private val videoDao: VideoStorageDao)
    :VideoStorageRepository {

    @SuppressLint("CheckResult")
    override fun update(videoStorage: VideoStorage) {
        videoDao.update(videoStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.i("update success")
            }, {
                Timber.i("error")
            })
    }

    @SuppressLint("CheckResult")
    override fun delete(videoStorage: VideoStorage) {
        videoDao.delete(videoStorage)
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.i("delete success")
            }, {
                Timber.i("error")
            })
    }

    @SuppressLint("CheckResult")
    override fun deleteAllVideoStorage() {
        Completable.fromAction { videoDao.deleteAllVideoStorage() }
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.i("delete all success")
            }, {
                Timber.i("error")
            })
    }

    override fun getAllVideoStorage(): Observable<List<VideoStorage>> =
        videoDao.getAllVideoStorage()
}