package kr.co.jsh.customview

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ObservableFloat
import kotlinx.android.synthetic.main.include_view_trimmer.view.*
import kr.co.jsh.R
import kr.co.jsh.interfaces.OnProgressVideoListener
import kr.co.jsh.interfaces.OnTrimVideoListener
import kr.co.jsh.interfaces.OnVideoListener
import kr.co.jsh.utils.*
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.lang.Math.ceil
import java.util.*
import kotlin.collections.ArrayList


class VideoTrimmer @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null


    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoListener> = ArrayList()

    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnVideoListener: OnVideoListener? = null

    private lateinit var mBitmaps: LongSparseArray<Bitmap>

    private var mDuration = 0f
    private var mTimeVideo = 0f
    private var mStartPosition = 0f

    private var mEndPosition = 0f
    private var mResetSeekBar = true

    private var destinationPath: String get() {
        if (mFinalPath == null) {
            val folder = Environment.getExternalStorageDirectory()
            mFinalPath = folder.path + File.separator
        }
        return mFinalPath ?: ""
    }
        set(finalPath) {
            mFinalPath = finalPath
        }

    init {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.include_view_trimmer, this, true)
        handlerTop.progress = handlerTop.max / 2
        handlerTop.isEnabled = false
        
        setUpListeners()
        //setUpMargins()
        icon_video_play.setOnClickListener {
            Log.i("video_loader.isPlaying","${video_loader.isPlaying}")
            if (video_loader.isPlaying) {
                //icon_video_play.visibility = View.VISIBLE
                //mMessageHandler.removeMessages(SHOW_PROGRESS)
                icon_video_play.isSelected = false
                video_loader.pause()

                Log.i("video stop","")
            } else {
                if (mResetSeekBar) {
                    mResetSeekBar = false
                    video_loader.seekTo(mStartPosition.toInt())
                }
                icon_video_play.isSelected = true
                video_loader.start()
                var thread = ThreadClass()
                //---------
                thread.start()

                Log.i("video start","")

            }

        }
        video_loader.setOnCompletionListener {
            icon_video_play.isSelected = false
        }

//        timeLineView.setOnTouchListener{ _: View, motionEvent: MotionEvent ->
//            when(motionEvent.action) {
//                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP-> Log.i("scroll X", "${scroll.scrollX}")
//                textStartTime.text =
//                    String.format( "%s", TrimVideoUtils.stringForTime((mDuration * scroll.scrollX)/timeLineView.width))
//            }
//            true
//            }
    }

    inner class ThreadClass:Thread(){

        override fun run() {
            while (video_loader.isPlaying) {

                context.runOnUiThread {
                    textStartTime.text = String.format(
                        "%s",
                        TrimVideoUtils.stringForTime(video_loader.currentPosition.toFloat())
                    )
                    //시간 흐를때마다 뷰 옆으로 이동!
                    scroll.scrollTo ((video_loader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels))/video_loader.duration, 0)
                }
                sleep(1)
            }
        }
    }

    private fun setUpListeners() {
        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Float, max: Float, scale: Float) {
                updateVideoProgress(time)
            }
        })

        video_loader.setOnErrorListener { _, what, _ ->
            mOnTrimVideoListener?.onError("Something went wrong reason : $what")
            false
        }

        video_loader.setOnPreparedListener { mp -> onVideoPrepared(mp) }
        video_loader.setOnCompletionListener { onVideoCompleted() }
    }

    fun onSaveClicked() {
        mOnTrimVideoListener?.onTrimStarted()
        icon_video_play.visibility = View.VISIBLE
        video_loader.pause()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        val metaDataKeyDuration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

        val file = File(mSrc.path ?: "")

        if (mTimeVideo < MIN_TIME_FRAME) {
            if (metaDataKeyDuration - mEndPosition > MIN_TIME_FRAME - mTimeVideo) mEndPosition += MIN_TIME_FRAME - mTimeVideo
            else if (mStartPosition > MIN_TIME_FRAME - mTimeVideo) mStartPosition -= MIN_TIME_FRAME - mTimeVideo
        }

        val root = File(destinationPath)
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
        VideoOptions(context).trimVideo(TrimVideoUtils.stringForTime(mStartPosition), TrimVideoUtils.stringForTime(mEndPosition), file.path, outPutPath, outputFileUri, mOnTrimVideoListener)
    }

    fun onCancelClicked() {
        video_loader.stopPlayback()
        mOnTrimVideoListener?.cancelAction()
    }

    fun frameCapture():Bitmap {

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        var currentPosition: Long
        try {
            currentPosition=video_loader.currentPosition.toLong()
            //currentPosition = frame_location!!.toLong() //현재 위치 프레임
        } catch (e: NullPointerException) {
            currentPosition = 0L
        }
        Toast.makeText(
            context,
            "Current Position: $currentPosition (ms)",
            Toast.LENGTH_LONG
        ).show()

        var bmFrame: Bitmap = mediaMetadataRetriever.getFrameAtTime(currentPosition* 1000L)

        if (bmFrame == null) {
            Toast.makeText(
                context,
                "bmFrame == null!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.i("bmFrame", " 통과")


        }
        return bmFrame
    }

    fun setVideoInformationVisibility(visible: Boolean): VideoTrimmer {
        // timeFrame.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        //val screenWidth = layout_surface_view.width
        //val screenHeight = layout_surface_view.height
        // val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = video_loader.layoutParams
        Log.i("video size","width : ${videoWidth} and height: ${videoHeight}")

        handlerTop.visibility=View.VISIBLE

        video_loader.layoutParams = lp

        icon_video_play.visibility = View.VISIBLE

        mDuration = video_loader.duration.toFloat()
        setSeekBarPosition()

        setTimeFrames()
        //timeFrame : 프레임이 시각적으로 나옴.

        mOnVideoListener?.onVideoPrepared()
    }

    private fun setSeekBarPosition() { //동영상 길이 범위 (왼쪽,오른쪽) 어떻게 할지 정하는 것
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2

            }
            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2

            }
            else -> {
                mStartPosition = 0f
                mEndPosition = mDuration
            }
        }
        video_loader.seekTo(mStartPosition.toInt())
        mTimeVideo = mDuration
    }

    private fun setTimeFrames() {
         textTimeSelection.text = String.format("%s", TrimVideoUtils.stringForTime(mEndPosition))
        //rxTextView 적용 예정
        //textStartTime.text= String.format("%s", TrimVideoUtils.stringForTime(mStartPosition) )
        textStartTime.text = String.format(
            "%s",
            TrimVideoUtils.stringForTime(video_loader.currentPosition.toFloat())
        )


        timeLineView.setOnTouchListener{ _: View, motionEvent: MotionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
//                    Log.i("scroll X", "${scroll.scrollX}")
                    var touch_time = ObservableFloat()
                    touch_time.set ((mDuration * scroll.scrollX) / (timeLineView.width - ScreenSizeUtil(context).widthPixels))
                    textStartTime.text = String.format(
                        "%s",
                        TrimVideoUtils.stringForTime(touch_time.get())
                    )
                    video_loader.seekTo(touch_time.get().toInt())
                    Log.i("seekto","${touch_time.get()}")

                    true
                }

                else -> false
            }
        }
    }

    private fun onVideoCompleted() {
        video_loader.seekTo(mStartPosition.toInt())
    }

    private fun updateVideoProgress(time: Float) {
        if (video_loader == null) return
        handlerTop.visibility = View.VISIBLE
        if (time >= mEndPosition) {
            //mMessageHandler.removeMessages(SHOW_PROGRESS)
            video_loader.pause()
            icon_video_play.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
    }

    fun setOnTrimVideoListener(onTrimVideoListener: OnTrimVideoListener): VideoTrimmer {
        mOnTrimVideoListener = onTrimVideoListener
        return this
    }

    fun setOnVideoListener(onVideoListener: OnVideoListener): VideoTrimmer {
        mOnVideoListener = onVideoListener
        return this
    }

    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")

    }

    fun setDestinationPath(path: String): VideoTrimmer {
        destinationPath = path
        return this
    }

    fun setVideoURI(videoURI: Uri): VideoTrimmer {
        mSrc = videoURI
        video_loader.setVideoURI(mSrc)
        video_loader.requestFocus()
        mBitmaps = getThumbnailList()
        timeLineView.drawView(mBitmaps)
        return this
    }

    private fun getThumbnailList(): LongSparseArray<Bitmap> {
        val thumbnailList = LongSparseArray<Bitmap>()

        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    //val threshold = 10
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mSrc)
                    //동영상의 총 길이

                    //frameWidth/Height = video_loader 의 너비,높이
                    //initialBitmap = 불러온 비디오 프레임의 bitmap
                    //cropWidth/height = timelineview에 하나씩 붙일 , 리사이즈된 프레임의 너비 높이
                    //numThumbs = 썸네일 보여줄 갯수
                    val videoLengthInMs = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))).toLong()
                    var numThumbs = if(videoLengthInMs>=3000) ceil(videoLengthInMs/3000.0).toInt() else 1
                    val cropHeight = 150 //timelineview에서 한 프레임의 너비 (동적으로 변경되게끔 코드 수정해야함!)
                    val cropWidth = ScreenSizeUtil(context).widthPixels/4 //timelineview에서 한 프레임의 너비

                    //val interval = videoLengthInMs / numThumbs
                    val interval = if(videoLengthInMs< 3000) videoLengthInMs else 3000
                    Log.i("test","${numThumbs}")

                    for (i in 0 until numThumbs) {
                        var bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        if (bitmap != null) {
                            try {
                                //frameWidth, frameHeight 이 0으로 나오는 오류가 발생함.
//                                bitmap = Bitmap.createScaledBitmap(bitmap, frameWidth, frameHeight, false)
                                bitmap = Bitmap.createScaledBitmap(bitmap, cropWidth, cropHeight, false)
                                //bitmap = Bitmap.createBitmap(bitmap,0,0, cropWidth, cropHeight)
                                Log.i("bitmap111","${bitmap.width}, ${bitmap.height}")


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            thumbnailList.put(i.toLong(), bitmap)
                            Log.i("1:","${thumbnailList.size()}")

                        }
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                }
                Log.i("return 직전:","${thumbnailList.size()}")
            }
        })
        return thumbnailList
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
    }

}
