package kr.co.domain.koin.repository.room;

import io.reactivex.Observable
import kr.co.data.entity.room.ResultFileStorage

interface ResultFileStorageRepository {

    fun insert(fileStorage: ResultFileStorage)

    fun update(fileStorage: ResultFileStorage)

    fun delete(fileStorage: ResultFileStorage)

    fun deleteAllStorage()

    fun getAllStorage() : Observable<List<ResultFileStorage>>
}