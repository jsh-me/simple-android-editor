package kr.co.domain.koin.repositoryimpl.room

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.api.room.ResultFileStorageDao
import kr.co.domain.koin.repository.room.ResultFileStorageRepository
import timber.log.Timber

class ResultFileStorageRepositoryImpl(private val resultFileStorageDao: ResultFileStorageDao)
    : ResultFileStorageRepository {

    @SuppressLint("CheckResult")
    override fun insert(fileStorage: ResultFileStorage) {
        resultFileStorageDao.insert(fileStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("insert success")
            },{
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun update(fileStorage: ResultFileStorage) {
        resultFileStorageDao.update(fileStorage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("update success")
            },{
                Timber.e(it.localizedMessage)

            })
    }

    @SuppressLint("CheckResult")
    override fun delete(fileStorage: ResultFileStorage) {
        resultFileStorageDao.delete(fileStorage)
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
        Completable.fromAction{ resultFileStorageDao.deleteAllImageStorage()}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.e("delete all success")
            },{
                Timber.e(it.localizedMessage)
            })
    }

    override fun getAllImageStorage(): Observable<List<ResultFileStorage>> =
        resultFileStorageDao.getAllImageStorage()
}