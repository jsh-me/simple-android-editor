package kr.co.data.entity.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "result_storage_table")
data class ResultFileStorage(@PrimaryKey(autoGenerate = true) var id: Int?,
                        @ColumnInfo(name = "result_path") var path: String,
                        @ColumnInfo(name = "file_name") var filename: String,
                        @ColumnInfo(name = "fileType") var fileType: String)