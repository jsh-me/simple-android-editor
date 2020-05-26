package kr.co.jsh.utils.permission_verQ

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kr.co.jsh.utils.RunOnUiThread
import java.io.*
import kotlin.concurrent.thread

object ScopeStorageFileUtil{

    //갤러리로 저장하기
    fun addPhotoAlbum(bitmap: Bitmap, displayName: String, mimeType: String, compressFormat: Bitmap.CompressFormat, context: Context) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream)
                outputStream.close()
                Toast.makeText(context, "Add bitmap to album succeeded.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addVideoAlbum(src: Uri, context: Context) {
        RunOnUiThread(context).safely {
            // Toast.makeText(context, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
            //Todo override 된 함수에 넣어줌 ( 사용자가 자른 동영상 )

            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, src)
            val displayName = "${System.currentTimeMillis()}.mp4"
            //Android Q 이상 대응
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                values.put(MediaStore.Video.Media.IS_PENDING, 1)

                val collection =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val fileUri = context.contentResolver.insert(collection, values)
                context.contentResolver.openFileDescriptor(fileUri!!, "w", null).use {
                    // write something to OutputStream
                    context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                        descriptor?.let {
                            FileOutputStream(descriptor.fileDescriptor).use { out ->
                                val videoFile = File(src.toString())
                                FileInputStream(videoFile).use { inputStream ->
                                    val buf = ByteArray(8192)
                                    while (true) {
                                        val sz = inputStream.read(buf)
                                        if (sz <= 0) break
                                        out.write(buf, 0, sz)
                                    }
                                }
                            }
                        }
                    }
                }
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(fileUri, values, null, null)
            }

            //그 이외
            else {
                val duration =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        .toLong()
                val width =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        .toLong()
                val height =
                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        .toLong()
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, src.path)
                values.put(MediaStore.Video.VideoColumns.DURATION, duration)
                values.put(MediaStore.Video.VideoColumns.WIDTH, width)
                values.put(MediaStore.Video.VideoColumns.HEIGHT, height)
                val id = ContentUris.parseId(
                    context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                )
                Log.e("VIDEO ID", id.toString())
            }

        }
    }


    //갤러리로 이동하기
    fun pickFileAndCopyUriToExternalFilesDir() : Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        return intent
    }

    //uri로 파일 이름 구하기
    fun getFileNameByUri(uri: Uri, context: Context): String {
        var fileName = System.currentTimeMillis().toString()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            cursor.close()
        }
        return fileName
    }

    //복사하기
    fun  copyUriToExternalFilesDir(uri: Uri, fileName: String, context: Context) {
        thread {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempDir = context.getExternalFilesDir("temp")
            if (inputStream != null && tempDir != null) {
                val file = File("$tempDir/$fileName")
                val fos = FileOutputStream(file)
                val bis = BufferedInputStream(inputStream)
                val bos = BufferedOutputStream(fos)
                val byteArray = ByteArray(1024)
                var bytes = bis.read(byteArray)
                while (bytes > 0) {
                    bos.write(byteArray, 0, bytes)
                    bos.flush()
                    bytes = bis.read(byteArray)
                }
                bos.close()
                fos.close()
                    Toast.makeText(context, "Copy file into $tempDir succeeded.", Toast.LENGTH_LONG).show()
            }
        }
    }


}