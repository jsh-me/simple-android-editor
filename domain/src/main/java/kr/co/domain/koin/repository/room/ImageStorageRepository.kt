package kr.co.domain.koin.repository.room;

import io.reactivex.Observable
import kr.co.data.entity.room.ImageStorage

interface ImageStorageRepository {

    fun insert(imageStorage:ImageStorage)

    fun update(imageStorage:ImageStorage)

    fun delete(imageStorage:ImageStorage)

    fun deleteAllImageStorage()

    fun getAllImageStorage() : Observable<List<ImageStorage>>

}