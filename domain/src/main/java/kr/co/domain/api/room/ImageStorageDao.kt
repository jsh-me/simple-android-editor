package kr.co.domain.api.room

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import kr.co.data.entity.room.ImageStorage

@Dao
interface ImageStorageDao {

    @Insert
    fun insert(imageStorage: ImageStorage) : Completable

    @Update
    fun update(imageStorage: ImageStorage) : Completable

    @Delete
    fun delete(imageStorage: ImageStorage) : Completable

    @Query("DELETE FROM image_storage_table")
    fun deleteAllImageStorage()

    @Query("SELECT * FROM image_storage_table ORDER BY id DESC")
    fun getAllImageStorage(): Observable<List<ImageStorage>>
}