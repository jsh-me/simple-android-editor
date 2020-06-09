package kr.co.domain.api.usecase

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.koin.repository.room.ResultFileStorageRepository

class AllLoadFileDataBaseUseCase(resultFileStorageRepository: ResultFileStorageRepository){
    private val resultFileStorageRepository = resultFileStorageRepository

    fun allLoad(): Observable<List<ResultFileStorage>> =
        resultFileStorageRepository.getAllStorage()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}