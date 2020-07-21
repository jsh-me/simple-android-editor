package kr.co.jsh.utils.videoUtil

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.google.android.exoplayer2.offline.Download
import kr.co.jsh.feature.videoedit.TrimmerContract
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class VideoOptions(private var ctx: Context) {

    @SuppressLint("SimpleDateFormat")
    fun trimVideo(startPosition: String, endPosition: String, inputPath: String, listener: TrimmerContract.View) {
        val ff = FFmpeg.getInstance(ctx)
        val date = "${SimpleDateFormat("HH_mm_ss").format(Date(System.currentTimeMillis()))}.mp4"
        val filePath = "/storage/emulated/0/DCIM/$date"
        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Timber.e("onFinish")
            }
            override fun onSuccess() {
                Timber.e("onSuccess")

                //val command = arrayOf("-y", "-i", inputPath, "-ss", startPosition, "-to", endPosition, "-c", "copy", outputPath)
                val command =
                    if(startPosition=="00:0.00")
                        arrayOf("-y", "-i", inputPath,"-vcodec","copy","-acodec","copy","-t", endPosition, filePath)
                    else
                        arrayOf("-y", "-i", inputPath, "-ss", startPosition, "-to", endPosition, "-vcodec","copy","-acodec","copy", filePath)

                Timber.e("startPosition: $startPosition / endPosition: $endPosition")
                try {
                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
                        override fun onSuccess(message: String?) {
                            super.onSuccess(message)
                            Timber.e("onSuccess: ${message!!}")
                        }

                        override fun onProgress(message: String?) {
                            super.onProgress(message)
                            Timber.e("onProgress: ${message!!}")
                        }

                        override fun onFailure(message: String?) {
                            super.onFailure(message)
                            listener.onError(message.toString())
                            Timber.e("onFailure: ${message!!}")
                        }

                        override fun onStart() {
                            super.onStart()
                            Timber.e("start :")
                        }

                        override fun onFinish() {
                            super.onFinish()
                            listener.getResult(filePath.toUri())
                            Timber.e("finish!")
                        }
                    })
                } catch (e: FFmpegCommandAlreadyRunningException) {
                    listener.onError(e.toString())
                }
            }

            override fun onFailure() {
                Timber.e("Load Failed")
                listener.onError("Failed")
            }

            override fun onStart() {
            }
        })
        listener.onTrimStarted()
    }

//    fun cropVideo(width: Int, height: Int, x: Int, y: Int, inputPath: String, outputPath: String, outputFileUri: Uri, listener: OnCropVideoListener?, frameCount: Int) {
//        val ff = FFmpeg.getInstance(ctx)
//        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
//            override fun onFinish() {
//                Log.e("FFmpegLoad", "onFinish")
//            }
//
//            override fun onSuccess() {
//                Log.e("FFmpegLoad", "onSuccess")
//                val command = arrayOf("-i", inputPath, "-filter:v", "crop=$width:$height:$x:$y", "-threads", "5", "-preset", "ultrafast", "-strict", "-2", "-c:a", "copy", outputPath)
//                try {
//                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
//                        override fun onSuccess(message: String?) {
//                            super.onSuccess(message)
//                            Log.e(TAG, "onSuccess: " + message!!)
//                        }
//
//                        override fun onProgress(message: String?) {
//                            super.onProgress(message)
//                            if (message != null) {
//                                val messageArray = message.split("frame=")
//                                if (messageArray.size >= 2) {
//                                    val secondArray = messageArray[1].trim().split(" ")
//                                    if (secondArray.isNotEmpty()) {
//                                        val framesString = secondArray[0].trim()
//                                        try {
//                                            val frames = framesString.toInt()
//                                            val progress = (frames.toFloat() / frameCount.toFloat()) * 100f
//                                            listener?.onProgress(progress)
//                                        } catch (e: Exception) {
//                                        }
//                                    }
//                                }
//                            }
//                            Log.e(TAG, "onProgress: " + message!!)
//                        }
//
//                        override fun onFailure(message: String?) {
//                            super.onFailure(message)
//                            listener?.onError(message.toString())
//                            Log.e(TAG, "onFailure: " + message!!)
//                        }
//
//                        override fun onStart() {
//                            super.onStart()
//                            Log.e(TAG, "onStart: ")
//                        }
//
//                        override fun onFinish() {
//                            super.onFinish()
//                            listener?.getResult(outputFileUri)
//                            Log.e(TAG, "onFinish: ")
//                        }
//                    })
//                } catch (e: FFmpegCommandAlreadyRunningException) {
//                    listener?.onError(e.toString())
//                }
//            }
//
//            override fun onFailure() {
//                Log.e("FFmpegLoad", "onFailure")
//                listener?.onError("Failed")
//            }
//
//            override fun onStart() {
//            }
//        })
//        listener?.onCropStarted()
//    }

//    fun compressVideo(inputPath: String, outputPath: String, outputFileUri: Uri, width: String, height: String, listener: OnCompressVideoListener?) {
//        val ff = FFmpeg.getInstance(ctx)
//        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
//            override fun onFinish() {
//                Log.e("FFmpegLoad", "onFinish")
//            }
//
//            override fun onSuccess() {
//                Log.e("FFmpegLoad", "onSuccess")
//                val command = arrayOf("-i", inputPath, "-vf", "scale=$width:$height", outputPath) //iw:ih
//                try {
//                    ff.execute(command, object : ExecuteBinaryResponseHandler() {
//                        override fun onSuccess(message: String?) {
//                            super.onSuccess(message)
//                            Log.e(TAG, "onSuccess: " + message!!)
//                        }
//
//                        override fun onProgress(message: String?) {
//                            super.onProgress(message)
//                            listener?.onError(message.toString())
//                            Log.e(TAG, "onProgress: " + message!!)
//                        }
//
//                        override fun onFailure(message: String?) {
//                            super.onFailure(message)
//                            listener?.onError(message.toString())
//                            Log.e(TAG, "onFailure: " + message!!)
//                        }
//
//                        override fun onStart() {
//                            super.onStart()
//                            Log.e(TAG, "onStart: ")
//                        }
//
//                        override fun onFinish() {
//                            super.onFinish()
//                            listener?.getResult(outputFileUri)
//                            Log.e(TAG, "onFinish: ")
//                        }
//                    })
//                } catch (e: FFmpegCommandAlreadyRunningException) {
//                    listener?.onError(e.toString())
//                }
//            }
//
//            override fun onFailure() {
//                Log.e("FFmpegLoad", "onFailure")
//                listener?.onError("Failed")
//            }
//
//            override fun onStart() {
//            }
//        })
//        listener?.onCompressStarted()
//    }
}