package kr.co.domain.api.room

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.co.data.entity.room.ImageStorage
import kr.co.data.entity.room.VideoStorage

@Database(entities = [VideoStorage::class, ImageStorage::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoStorageDao() : VideoStorageDao
    abstract fun imageStorageDao() : ImageStorageDao

}
