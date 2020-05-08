package kr.co.jsh.customview

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ObservableFloat
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.include_view_trimmer.view.*
import kotlinx.coroutines.*
import kr.co.jsh.R
import kr.co.jsh.localclass.PauseableDispatcher
import kr.co.jsh.interfaces.OnProgressVideoListener
import kr.co.jsh.interfaces.OnTrimVideoListener
import kr.co.jsh.interfaces.OnVideoListener
import kr.co.jsh.utils.*
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import java.lang.Math.ceil
import java.util.*
import kotlin.Pair
import kotlin.collections.ArrayList


class VideoTrimmer @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    private var touch_time = ObservableFloat()
    private var timeposition =0
    private val dispatcher =
        PauseableDispatcher(Handler(Looper.getMainLooper()))
    
    //자른 횟수
    private var crop_count = 0

    // crop 거리(crop_x1. crop_x2) , current-position
    // 두번 자르면 총 4개의 key-value가 나와야 함
    lateinit var crop_time : ArrayList<Pair<Int, Int>>

    //border view 의 크기를 만들어줌
    lateinit var params : FrameLayout.LayoutParams

    //비디오의 전체화면
    private var isFull = false

    private var bitmapArrayList = LongSparseArray<Bitmap>()
    private var crop_x1 = 0
    private var crop_x2 = 0

    //private var thread = ThreadClass()


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

        crop_time = arrayListOf() //initialize
        crop_time.add(Pair(0, 0))//1

        setUpListeners()
        //setUpMargins()
        icon_video_play.setOnClickListener {

            Log.i("current position","${video_loader.currentPosition}")
            if (video_loader.isPlaying) {
                //icon_video_play.visibility = View.VISIBLE
                //mMessageHandler.removeMessages(SHOW_PROGRESS)

                icon_video_play.isSelected = false
                timeposition = video_loader.currentPosition
                video_loader.seekTo(timeposition)
                video_loader.pause()
                dispatcher.pause()

            } else {
                icon_video_play.isSelected = true
                video_loader.seekTo(timeposition)
                video_loader.start()
               // thread.start()
                start_thread()
                dispatcher.resume()

            }

        }
        video_loader.setOnCompletionListener {
            icon_video_play.isSelected = false
        }
    }

//    inner class ThreadClass:Thread(){
//
//        override fun run() {
//            while (video_loader.isPlaying) {
//
//                context.runOnUiThread {
//                    textStartTime.text = String.format(
//                        "%s",
//                        TrimVideoUtils.stringForTime(video_loader.currentPosition.toFloat())
//                    )
//                    //시간 흐를때마다 뷰 옆으로 이동!
//                    scroll.scrollTo ((video_loader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels))/video_loader.duration, 0)
//                }
//                sleep(1)
//            }
//        }
//    }

    fun start_thread(){
        GlobalScope.launch(dispatcher) {
            if (this.isActive) {
                suspendFunc()
            }
        }
    }
    suspend fun suspendFunc() {
        while (video_loader.isPlaying) {
                context.runOnUiThread {
                    textStartTime.text = String.format(
                        "%s",
                        TrimVideoUtils.stringForTime(video_loader.currentPosition.toFloat())
                    )
                    //시간 흐를때마다 뷰 옆으로 이동!
                    scroll.scrollTo ((video_loader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels))/video_loader.duration, 0)
                }
                delay(1)
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

        remove_btn.setOnClickListener { removeMode() }
        reset_paint_btn.setOnClickListener { clearDraw() }
        fullscreen_btn.setOnClickListener { fullScreen() }

    }

    private fun fullScreen(){
        setFullScreen(!isFull)
    }

    private fun setFullScreen(full: Boolean){
        isFull = full

    }

    private fun removeMode(){
        video_frame_view.setBackgroundResource(R.color.background_space)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        var bitmap = mediaMetadataRetriever.getFrameAtTime(touch_time.get().toLong() * 1000 , MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        video_frame_view.setBackgroundImage(bitmap, BackgroundType.BITMAP, BackgroundScale.CENTER_INSIDE)

        hideVideoView()
    }

    private fun showVideoView(){
        if(video_loader.visibility == View.INVISIBLE) {
            video_loader.visibility = View.VISIBLE
            video_frame_view.visibility = View.INVISIBLE
            clearDraw()
        }
    }

    private fun hideVideoView(){
        if(video_loader.visibility == View.VISIBLE) {
            video_loader.visibility = View.INVISIBLE
            video_frame_view.visibility = View.VISIBLE
        }
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
                //편집할 영역을 선택하기
                MotionEvent.ACTION_UP,MotionEvent.ACTION_DOWN -> {
                    try {
                        if(crop_count == 2) {
                            setBoarderRange(motionEvent.x)
                            Log.i("touch x coordi:", "${motionEvent.x}")
                            true
                        }
                        else { false }
                    }
                    catch(e:Exception){
                        false
                    }
                }
                else -> false
            }
        }

        scroll.setOnScrollChangeListener{ view: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(scrollX != oldScrollX && !video_loader.isPlaying){
                Observable.just(scroll.scrollX)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showVideoView() //재생 버튼 누른 후 remove 하면 이상하게 동작함. 무조건 seekbar로 이동시에만 정상동작.

                        touch_time.set((mDuration * it) / ((timeLineView.width) - ScreenSizeUtil(context).widthPixels))
                        video_loader.seekTo(touch_time.get().toInt())
                        textStartTime.text = String.format(
                            "%s",
                            TrimVideoUtils.stringForTime(touch_time.get())
                        )
                        Timber.i("test!!")
                    }, {
                        Timber.i(it.localizedMessage)
                    })
            }
        }


        crop_btn.setOnClickListener {
            Log.i("size:", "${crop_time.size}")
            crop_count++

            when (crop_count) {
                1 -> {
                    bitmapArrayList = mBitmaps
                    crop_x1 =
                        (video_loader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels)) / video_loader.duration
                    //미리 두번 넣고 추후에 수정하자.
                    crop_time.add(Pair(crop_x1, video_loader.currentPosition))//2
                    Log.i("size:", "${crop_time.size}")
                    crop_time.add(Pair(crop_x1, video_loader.currentPosition))//3
                    Log.i("size:", "${crop_time.size}")

                    timeLineView.cropView(bitmapArrayList, crop_x1, crop_x2, crop_count)
                }
                2 -> {
                    crop_x2 =
                        (video_loader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels)) / video_loader.duration

                    //요렇게하면 crop_time은 좌표값이 작은 순부터 큰 순으로 자동정렬 되겠지
                    if (crop_x1 < crop_x2) {
                        crop_time[2] = Pair(crop_x2, video_loader.currentPosition)
                    } else {
                        crop_time[1] = Pair(crop_x2, video_loader.currentPosition)
                    }

                    crop_time.add(
                        Pair(
                            timeLineView.width - ScreenSizeUtil(context).widthPixels,
                            video_loader.duration
                        )
                    ) //4
                    timeLineView.cropView(bitmapArrayList, crop_x1, crop_x2, crop_count)
                    initialBordering()
                }
                else -> {
                    Toast.makeText(context, "CROP은 두 번만 가능 !", Toast.LENGTH_LONG).show()
                }
            }
        }

        reset.setOnClickListener {
            try {
                crop_count = 0
                crop_time.clear()
                crop_time.add(Pair(0, 0))//1

                timeLineView.resetView(crop_count)
                border.visibility = View.INVISIBLE
            }
            catch (e: Exception) {
                Toast.makeText(context, "잘라진 것이 없어요!", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun clearDraw(){
        video_frame_view.restartDrawing()
        removeMode()
    }


    //coordiX: 사용자가 터치한 좌표의 X값을 가져옴 (상대좌표)
    private fun setBoarderRange(coordiX:Float){
        //터치한 곳에서 width/2 를 빼야지 원활한 계산이 가능해짐
        var startX = coordiX - ScreenSizeUtil(context).widthPixels/2
        if(startX >= crop_time[1].first && startX <= crop_time[2].first){
            border.visibility = View.INVISIBLE
            //7은 TimeLintView 에서 그려줄 때 만든 margin 값
            params = FrameLayout.LayoutParams(crop_time[2].first - crop_time[1].first +7 , timeLineView.height)
            params.marginStart = ScreenSizeUtil(context).widthPixels/2 + crop_time[1].first
            border.layoutParams = params
            border.visibility = View.VISIBLE
        }
        else if(startX > crop_time[2].first){
            border.visibility = View.INVISIBLE
            params = FrameLayout.LayoutParams(crop_time[3].first - crop_time[2].first +14 , timeLineView.height)
            params.marginStart = ScreenSizeUtil(context).widthPixels/2 + crop_time[2].first
            border.layoutParams = params
            border.visibility = View.VISIBLE
        }
        else if (startX >= 0 && startX < crop_time[1].first) {
            border.visibility = View.INVISIBLE
            initialBordering()
        }
        else{
            border.visibility = View.INVISIBLE
        }

    }

    //두번 자르면, 자동으로 맨 처음 영역이 선택됨. 그 후에 setEditRange로 수정 가능
    private fun initialBordering(){
        params = FrameLayout.LayoutParams(crop_time[1].first, timeLineView.height)
        params.marginStart = ScreenSizeUtil(context).widthPixels/2
        border.layoutParams = params
        border.visibility = View.VISIBLE
        Toast.makeText(context, "편집할 영역을 선택하세요.", Toast.LENGTH_LONG).show()
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
                    val interval = if(videoLengthInMs< 3000) videoLengthInMs*1000 else 3000*1000
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
