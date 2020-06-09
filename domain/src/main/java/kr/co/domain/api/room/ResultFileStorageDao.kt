package kr.co.domain.api.room

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import kr.co.data.entity.room.ResultFileStorage

@Dao
interface ResultFileStorageDao {
    @Insert
    fun insert(fileStorage: ResultFileStorage) : Completable

    @Update
    fun update(fileStorage: ResultFileStorage) : Completable

    @Delete
    fun delete(fileStorage: ResultFileStorage) : Completable

    @Query("DELETE FROM result_storage_table")
    fun deleteAllStorage()

    @Query("SELECT * FROM result_storage_table ORDER BY id DESC")
    fun getAllStorage(): Observable<List<ResultFileStorage>>
}