package kr.co.data.entity.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_storage_table")
data class ImageStorage(@PrimaryKey(autoGenerate = true) var id: Int?,
                        @ColumnInfo(name = "result_image_path") var path: String,
                        @ColumnInfo(name = "file_name") var filename: String)