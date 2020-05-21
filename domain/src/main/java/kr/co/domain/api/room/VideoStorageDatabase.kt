package kr.co.domain.api.room

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.data.entity.room.VideoStorage

@Database(entities = [VideoStorage::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoStorageDao() : VideoStorageDao

}