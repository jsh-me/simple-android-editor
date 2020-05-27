package kr.co.domain.api.room

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import kr.co.data.entity.room.VideoStorage


@Dao
interface VideoStorageDao {

    @Insert
    fun insert(videoStorage: VideoStorage) : Completable

    @Update
    fun update(videoStorage: VideoStorage) : Completable

    @Delete
    fun delete(videoStorage: VideoStorage) : Completable

    @Query("DELETE FROM video_storage_table")
    fun deleteAllVideoStorage()

    @Query("SELECT * FROM video_storage_table ORDER BY id DESC")
    fun getAllVideoStorage(): Observable<List<VideoStorage>>
}