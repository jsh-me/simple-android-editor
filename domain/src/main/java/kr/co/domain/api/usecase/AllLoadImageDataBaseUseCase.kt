package kr.co.domain.api.usecase

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ImageStorage
import kr.co.domain.koin.repository.room.ImageStorageRepository

class AllLoadImageDataBaseUseCase(imageStorageRepository: ImageStorageRepository) {
    private val imageStorageRepository = imageStorageRepository

    fun allLoad(): Observable<List<ImageStorage>> =
        imageStorageRepository.getAllImageStorage()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}