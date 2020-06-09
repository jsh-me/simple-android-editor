package kr.co.domain.api.room

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.data.entity.room.ResultFileStorage

@Database(entities = [ResultFileStorage::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultFileStorageDao() : ResultFileStorageDao
}