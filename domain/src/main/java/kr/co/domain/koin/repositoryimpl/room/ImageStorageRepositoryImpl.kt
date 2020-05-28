package kr.co.domain.koin.repositoryimpl.room

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ImageStorage
import kr.co.domain.api.room.ImageStorageDao
import kr.co.domain.koin.repository.room.ImageStorageRepository
import timber.log.Timber

class ImageStorageRepositoryImpl(private val imageDao: ImageStorageDao)
    : ImageStorageRepository {

    @SuppressLint("CheckResult")
    override fun insert(imageStorage: ImageStorage) {
        imageDao.insert(imageStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("insert success")
            },{
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun update(imageStorage: ImageStorage) {
        imageDao.update(imageStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("update success")
            },{
                Timber.e(it.localizedMessage)

            })
    }

    @SuppressLint("CheckResult")
    override fun delete(imageStorage: ImageStorage) {
        imageDao.delete(imageStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("delete success")
            },{
                Timber.e(it.localizedMessage)

            })
    }

    @SuppressLint("CheckResult")
    override fun deleteAllImageStorage() {
        Completable.fromAction{ imageDao.deleteAllImageStorage()}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("delete all success")
            },{
                Timber.e(it.localizedMessage)

            })
    }

    override fun getAllImageStorage(): Observable<List<ImageStorage>> =
        imageDao.getAllImageStorage()
}