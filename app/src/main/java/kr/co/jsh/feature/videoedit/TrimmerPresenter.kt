package kr.co.jsh.feature.videoedit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.api.usecase.PostImproveVideoPidNumber
import kr.co.domain.api.usecase.PostVideoPidNumberAndInfoUseCase
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.domain.globalconst.PidClass
import kr.co.domain.utils.addFile
import kr.co.domain.utils.toastShort
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.*
import kr.co.jsh.utils.BitmapUtil.bitmapToFileUtil
import kr.co.jsh.utils.permission.RealPathUtil
import kr.co.jsh.utils.permission.ScopeStorageFileUtil
import kr.co.jsh.utils.videoUtil.TrimVideoUtils
import kr.co.jsh.utils.videoUtil.VideoOptions
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrimmerPresenter(override var view: TrimmerContract.View,
                       private var postFileUploadUseCase: PostFileUploadUseCase,
                       private var postPidNumberAndInfoUseCase: PostVideoPidNumberAndInfoUseCase,
                       private var postImproveVideoPidNumber: PostImproveVideoPidNumber) : TrimmerContract.Presenter{

    private var mplayer: SimpleExoPlayer ?= null
    private var playbackPosition = 0L
    private var currentWindow = 0 //재생곡의 순번
    private var playWhenReady = false
    private val mediaMetadataRetriever = MediaMetadataRetriever()


    override fun initPlayer(uri: Uri, context: Context) {
        mplayer = SimpleExoPlayer.Builder(context).build()
        val dataSourceFactory =
            DefaultDataSourceFactory(context ,"del.it")
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        mplayer!!.apply{
            prepare(mediaSource)
            seekTo(currentWindow, playbackPosition)
            playWhenReady = playWhenReady
            view.setPlayer(mplayer!!)
        }

        mediaMetadataRetriever.setDataSource(context, uri)
    }

    override fun getVideoListener() {
        mplayer?.addListener(object: Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    val realDurationMillis: Long = mplayer?.duration!!
                    view.setVideoDuration(realDurationMillis)
                } else if(playbackState == ExoPlayer.STATE_ENDED){
                    mplayer!!.seekTo(0)
                    view.onVideoFinished()
                }
                super.onPlayerStateChanged(playWhenReady, playbackState)
            }
        })
    }

    override fun releasePlayer() {
        mplayer?.let{
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            mplayer = null
        }
    }

    override fun isVideoPlay(whenReady: Boolean) {
        mplayer?.playWhenReady = whenReady
        view.setVideoPlayFlag(whenReady)
    }

    override fun getVideoCurrentPosition(): Float {
        return mplayer?.currentPosition!!.toFloat()
    }

    override fun setVideoSeekTo(currentPosition: Long) {
        mplayer?.seekTo(currentPosition)
    }

    override fun getFrameBitmap(sec: Long) {
        val bitmap = mediaMetadataRetriever.getFrameAtTime(sec * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        view.setDrawBitmap(bitmap)
    }

    //----------

    override fun setCuttingVideo(context: Context, cropCount: Int, trimVideoTimeList: ArrayList<Pair<Long, Long>>, recycler: RecyclerView){
        var firstTrim = 0L
        var secondTrim = 0L

        when(cropCount){
            1 -> {
                firstTrim =
                    ( mplayer?.currentPosition!! * (recycler.width - ScreenSizeUtil(context).widthPixels)) /  mplayer?.duration!!

                trimVideoTimeList.add(Pair(firstTrim, mplayer?.currentPosition!!))//2
                trimVideoTimeList.add(Pair(firstTrim, mplayer?.currentPosition!!))//3
                trimVideoTimeList.add(Pair(recycler.width - ScreenSizeUtil(context).widthPixels.toLong(), mplayer?.duration!!)) //4
            }
             2-> {
                 secondTrim =
                     ( mplayer?.currentPosition!! * (recycler.width - ScreenSizeUtil(context).widthPixels)) /  mplayer?.duration!!
                if(trimVideoTimeList[1].first > secondTrim) {
                    trimVideoTimeList[1] = Pair(secondTrim,  mplayer?.currentPosition!!)
                }
                 else trimVideoTimeList[2] = Pair(secondTrim,  mplayer?.currentPosition!!)
             }

            else -> {
                context.toastShort("두번만 선택 가능")
            }
        }
       view.setPairList(trimVideoTimeList)

    }

    //사용자가 자른 동영상이 갤러리와 서버 동시에 저장, 업로드 되는 메소드
    override fun getResultUri(uri: Uri, context: Context, option: String) {
        ScopeStorageFileUtil.addVideoAlbum(uri, context)

        if(option.equals(Consts.SUPER_RESOL)) { improveFile(uri) }
        else { uploadFile(uri.toString()) }
    }

    override fun preparePath(extraIntent: Intent) {
        var path =""
        extraIntent?.let{
            path =  it.getStringExtra(EXTRA_VIDEO_PATH)
            }
        view.setVideoPath(path)
    }

    override fun getCropArrayList(context:Context, trimVideoTimeList:  ArrayList<Pair<Long, Long>>) {
        try {
            trimVideoTimeList.clear()
            trimVideoTimeList.add(Pair(0,0))//1
            view.resetCropView()
        } catch (e: Exception) {
            context.toastShort("잘라진 것이 없어요!")
        }
    }

    override fun getThumbnailList(mSrc: Uri, context:Context) {
        val thumbnailList = ArrayList<Bitmap>()
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)

        val videoLengthInMs = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))).toLong()
        val cropHeight = 150 //timelineview에서 한 프레임의 너비 (동적으로 변경되게끔 코드 수정해야함!)
        val cropWidth = ScreenSizeUtil(context).widthPixels/4 //timelineview에서 한 프레임의 너비
        val interval = if(videoLengthInMs< 3000) videoLengthInMs else 3000


        for (i in 0 .. videoLengthInMs step interval) {
            var bitmap = mediaMetadataRetriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bitmap != null) {
                try {
                    bitmap = Bitmap.createScaledBitmap(bitmap, cropWidth, cropHeight, false)
                    Log.i("bitmap111","${bitmap.width}, ${bitmap.height}")


                } catch (e: Exception) {
                    e.printStackTrace()
                }
                thumbnailList.add(bitmap)
                Log.i("1:","${thumbnailList.size}")

            }
        }
        mediaMetadataRetriever.release()
        view.setThumbnailListView(thumbnailList)
    }

    override fun trimVideo(path: String, context:Context, mSrc: Uri,  start_sec: Int, end_sec: Int) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)

        val file = File(mSrc.path ?: "")
        val root = File(path)
        root.mkdirs()
        val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
        val outPutPath = RealPathUtil.realPathFromUriApi19(context, outputFileUri)
            ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + mSrc.path?.substring(mSrc.path!!.lastIndexOf("/") + 1)).absolutePath
        Log.e("SOURCE", file.path)
        Log.e("DESTINATION", outPutPath)

        val extractor = MediaExtractor()
        var frameRate = 24
        try {
            extractor.setDataSource(file.path)
            val numTracks = extractor.trackCount
            for (i in 0..numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }
        val duration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        Log.e("FRAME RATE", frameRate.toString())
        Log.e("FRAME COUNT", (duration / 1000 * frameRate).toString())
        VideoOptions(context)
            .trimVideo(TrimVideoUtils.stringForTime(start_sec.toFloat()), TrimVideoUtils.stringForTime(end_sec.toFloat()), file.path, outPutPath, outputFileUri, view)
    }


    //Todo 동영상과 사진 확장자를 업로드 할 수 있는 메소드
    @SuppressLint("CheckResult")
    override fun uploadFile(uri: String) {
        //val path = "file://" + Uri.parse(uri)
        val path = uri.addFile()
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("video/*"), Uri.parse(path).toFile() ))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 )
                {
                    UserObject.ResponseCode = it.status.toInt()
                    view.uploadSuccess(it.message)
                    PidClass.videoObjectPid = it.datas.objectPid
                }
                else {
                    view.uploadFailed(it.message)
                    //PidClass.videoObjectPid = it.datas.objectPid
                }
            },{
                view.uploadFailed(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun uploadMaskFile(bitmap: Bitmap, frameTimeSec:Float, context: Context) {
        val file = bitmapToFileUtil(bitmap, context)
        val path = file.toString().addFile()
        val request = MultipartBody.Part.createFormData("file", path , RequestBody.create(MediaType.parse("image/*"),Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 ) {
                    PidClass.videoMaskObjectPid = it.datas.objectPid
                    sendVideoResultToServerWithInfo(PidClass.videoMaskObjectPid, frameTimeSec, PidClass.videoObjectPid)
                }
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed("로그인 후 가능")
                view.cancelJob()

            })
    }

    @SuppressLint("CheckResult")
    private fun improveFile(uri: Uri){
        val path = uri.toString().addFile()
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("video/*"), Uri.parse(path).toFile() ))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 ) {
                    PidClass.videoObjectPid = it.datas.objectPid
                    requestImproveVideo(PidClass.videoObjectPid)
                }
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed("로그인 후 가능")
                view.cancelJob()

            })
    }


    @SuppressLint("CheckResult")
    private fun sendVideoResultToServerWithInfo(maskPid: String, frameSec: Float, videoPid: String) {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        val curTime = dateFormat.format(Date(time))

        postPidNumberAndInfoUseCase.postPidNumberAndInfo(maskPid, frameSec, Consts.DEL_OBJ ,videoPid, curTime)
            .subscribe({
               if(it.status.toInt() == 200) {
                   Timber.e("Complete Video Remove Request")
                   PidClass.topVideoObjectPid.add(it.datas.objectPid)
                   view.stopAnimation()

               }
               else Timber.e("ERROR ${it.status}")
            },{
                it.localizedMessage
            })
    }

    @SuppressLint("CheckResult")
    private fun requestImproveVideo(videoPid:String){
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        val curTime = dateFormat.format(Date(time))

        postImproveVideoPidNumber.PostImproveVideoPidNumber(Consts.SUPER_RESOL, videoPid, curTime)
            .subscribe({
               if(it.status.toInt() == 200) {
                   Timber.e("Complete Video Improve Request")
                   view.stopAnimation()
               }
                else Timber.e("ERROR ${it.status}")
            },{
                it.localizedMessage
            })
    }
}