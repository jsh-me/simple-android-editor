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
    override fun insert(videoStorage: VideoStorage) {
        videoDao.insert(videoStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("insert success")
            },{
                Timber.e(it.localizedMessage)

            })

    }

    @SuppressLint("CheckResult")
    override fun update(videoStorage: VideoStorage) {
        videoDao.update(videoStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("update success")
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun delete(videoStorage: VideoStorage) {
        videoDao.delete(videoStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("delete success")
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun deleteAllVideoStorage() {
        Completable.fromAction { videoDao.deleteAllVideoStorage() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("delete all success")
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    override fun getAllVideoStorage(): Observable<List<VideoStorage>> =
        videoDao.getAllVideoStorage()

}