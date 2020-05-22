package kr.co.data.entity.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_storage_table")
data class VideoStorage(@PrimaryKey(autoGenerate = true) var id: Int?,
                        @ColumnInfo(name = "result_video_path") var path: String,
                        @ColumnInfo(name = "file_name") var filename: String)