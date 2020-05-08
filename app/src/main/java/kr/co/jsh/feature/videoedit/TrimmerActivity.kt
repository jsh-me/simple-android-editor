package kr.co.jsh.feature.videoedit

import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.LongSparseArray
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityTrimmerBinding
import kr.co.jsh.globalconst.Consts.Companion.MIN_TIME_FRAME
import kr.co.jsh.interfaces.OnProgressVideoListener
import kr.co.jsh.localclass.PausableDispatcher
import kr.co.jsh.utils.*
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class TrimmerActivity : AppCompatActivity() , TrimmerContract.View {
    private lateinit var binding: ActivityTrimmerBinding
    private lateinit var progressDialog : VideoProgressIndeterminateDialog
    private lateinit var presenter : TrimmerPresenter
    val removeColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false,false))

   ///---------------------trimmer
    private var touch_time = ObservableFloat()
    private var timeposition = 0
    private val dispatcher =
        PausableDispatcher(Handler(Looper.getMainLooper()))

    //자른 횟수
    private var crop_count = 0

    // crop 거리(crop_x1. crop_x2) , current-position
    // 두번 자르면 총 4개의 key-value가 나와야 함
    lateinit var crop_time: ArrayList<Pair<Int, Int>>

    //border view 의 크기를 만들어줌
    lateinit var params: FrameLayout.LayoutParams

    //비디오의 전체화면
    private var isFull = false

    private var bitmapArrayList = LongSparseArray<Bitmap>()
    private var crop_x1 = 0
    private var crop_x2 = 0


    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null


    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoListener> = ArrayList()


    private lateinit var mBitmaps: LongSparseArray<Bitmap>

    private var mDuration = 0f
    private var mTimeVideo = 0f
    private var mStartPosition = 0f

    private var mEndPosition = 0f
    private var mResetSeekBar = true

    private var destinationPath: String
        get() {
            if (mFinalPath == null) {
                val folder = Environment.getExternalStorageDirectory()
                mFinalPath = folder.path + File.separator
            }
            return mFinalPath ?: ""
        }
        set(finalPath) {
            mFinalPath = finalPath
        }
    ///---------------------trimmer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Environment.getExternalStorageState()
        setupDatabinding()
        initView()
        setUpListeners()

    }

    private fun initView(){
        presenter = TrimmerPresenter(this)
        progressDialog = VideoProgressIndeterminateDialog(this, "Cropping Video. Please Wait...")

        setupPermissions(this) {
            val extraIntent = intent
            presenter.prepareVideoPath(extraIntent)
        }

        binding.handlerTop.progress =  binding.handlerTop.max / 2
        binding.handlerTop.isEnabled = false

        crop_time = arrayListOf() //initialize
        crop_time.add(Pair(0, 0))//1


    }

    override fun videoPath(path: String) {
        mSrc=Uri.parse(path)
        binding.videoLoader.setVideoURI(mSrc)
        binding.videoLoader.requestFocus()
        presenter.getThumbnailList(mSrc, this)

        setVideoInformationVisibility(true)
//                .setDestinationPath("/document/" + File.separator + "returnable" + File.separator + "Videos" + File.separator)

        setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator)
    }

    override fun setThumbnailListView(thumbnailList: LongSparseArray<Bitmap>) {
        mBitmaps = thumbnailList
        binding.timeLineView.drawView(mBitmaps)
    }

    private fun setUpListeners() {
        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Float, max: Float, scale: Float) {
                updateVideoProgress(time)
            }
        })

        binding.videoLoader.setOnErrorListener { _, what, _ ->
            onError("Something went wrong reason : $what")
            false
        }

        binding.videoLoader.setOnPreparedListener { mp -> onVideoPrepared(mp) }
        binding.videoLoader.setOnCompletionListener { onVideoCompleted() }

    }

    fun cropVideo(){
        crop_count ++
        presenter.crop(this, crop_time, crop_count, mBitmaps, binding.timeLineView, binding.videoLoader)
    }

    fun resetTimeLineView(v: View){
        crop_count = 0
        presenter.resetCrop(this, crop_time)

        removeColor.set(arrayOf(false,false,false,false))
        removeColor.set(arrayOf(false,false,true,false))
    }

    override fun initialBordering(crop_time: ArrayList<Pair<Int, Int>>) {
        params = FrameLayout.LayoutParams(crop_time[1].first,  binding.timeLineView.height)
        params.marginStart = ScreenSizeUtil(this).widthPixels/2
        binding.border.layoutParams = params
        binding.border.visibility = View.VISIBLE
        Toast.makeText(this, "편집할 영역을 선택하세요.", Toast.LENGTH_LONG).show()
    }


    fun playVideo() {
        if(binding.videoLoader.visibility == View.INVISIBLE) {
            showVideoView() //draw상태에서 다시 재생하려고 할때 발생
            clearDraw()
        }

        if (binding.videoLoader.isPlaying) {

            binding.iconVideoPlay.isSelected = false
            timeposition = binding.videoLoader.currentPosition
            binding.videoLoader.seekTo(timeposition)
            binding.videoLoader.pause()
            dispatcher.pause()

        } else {
            binding.iconVideoPlay.isSelected = true
            binding.videoLoader.seekTo(timeposition)
            binding.videoLoader.start()
            startThread()
            dispatcher.resume()

        }
        binding.videoLoader.setOnCompletionListener{
            binding.iconVideoPlay.isSelected = false
        }
    }

    private fun startThread() {
        GlobalScope.launch(dispatcher) {
            if (this.isActive) {
                suspendFunc()
            }
        }
    }

    private suspend fun suspendFunc() {
        while ( binding.videoLoader.isPlaying) {
            applicationContext.runOnUiThread {
                binding.textStartTime.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime( binding.videoLoader.currentPosition.toFloat())
                )
                //시간 흐를때마다 뷰 옆으로 이동!
                binding.scroll.scrollTo(
                    ( binding.videoLoader.currentPosition * (binding.timeLineView.width - ScreenSizeUtil(
                        applicationContext
                    ).widthPixels)) /  binding.videoLoader.duration, 0
                )
            }
            delay(1)
        }
    }


    fun fullScreen(){
        setFullScreen(!isFull)
    }

    private fun setFullScreen(full: Boolean){
        isFull = full

    }

    private fun onVideoPrepared(mp: MediaPlayer) {
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        //val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        //val screenWidth = layout_surface_view.width
        //val screenHeight = layout_surface_view.height
        // val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = binding.videoLoader.layoutParams
        Log.i("video size","width : ${videoWidth} and height: ${videoHeight}")

        binding.handlerTop.visibility=View.VISIBLE
        binding.videoLoader.layoutParams = lp
        binding.iconVideoPlay.visibility = View.VISIBLE

        mDuration = binding.videoLoader.duration.toFloat()
        setSeekBarPosition()

        setTimeFrames()
        //timeFrame : 프레임이 시각적으로 나옴.

        onVideoPrepared()
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
        binding.videoLoader.seekTo(mStartPosition.toInt())
        mTimeVideo = mDuration
    }

    private fun setTimeFrames() {
        binding.textTimeSelection.text =
            String.format("%s", TrimVideoUtils.stringForTime(mEndPosition))
        //rxTextView 적용 예정
        //textStartTime.text= String.format("%s", TrimVideoUtils.stringForTime(mStartPosition) )
        binding.textStartTime.text = String.format(
            "%s",
            TrimVideoUtils.stringForTime(binding.videoLoader.currentPosition.toFloat())
        )


        binding.timeLineView.setOnTouchListener { _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                //편집할 영역을 선택하기
                MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> {
                    try {
                        if (crop_count == 2) {
                            setBoarderRange(motionEvent.x)
                            Log.i("touch x coordi:", "${motionEvent.x}")
                            true
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> false
            }
        }

        binding.scroll.setOnScrollChangeListener { view: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollX != oldScrollX && !binding.videoLoader.isPlaying) {
                Observable.just(binding.scroll.scrollX)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showVideoView() //재생 버튼 누른 후 remove 하면 이상하게 동작함. 무조건 seekbar로 이동시에만 정상동작.

                        touch_time.set(
                            (mDuration * it) / ((binding.timeLineView.width) - ScreenSizeUtil(
                                this
                            ).widthPixels)
                        )
                        binding.videoLoader.seekTo(touch_time.get().toInt())
                        binding.textStartTime.text = String.format(
                            "%s",
                            TrimVideoUtils.stringForTime(touch_time.get())
                        )
                        Timber.i("test!!")
                        timeposition = touch_time.get().toInt()

                    }, {
                        Timber.i(it.localizedMessage)
                    })
            }
        }

    }

    override fun resetCropView() {
        binding.timeLineView.resetView(crop_count)
        binding.border.visibility = View.INVISIBLE
    }


    fun clearDraw(){
        binding.videoFrameView.restartDrawing()
        removeMode()

        removeColor.set(arrayOf(false,false,false,false))
        removeColor.set(arrayOf(false,false,false,true))
    }

    //coordiX: 사용자가 터치한 좌표의 X값을 가져옴 (상대좌표)
    private fun setBoarderRange(coordiX:Float){
        //터치한 곳에서 width/2 를 빼야지 원활한 계산이 가능해짐
        var startX = coordiX - ScreenSizeUtil(this).widthPixels/2
        if(startX >= crop_time[1].first && startX <= crop_time[2].first){
            binding.border.visibility = View.INVISIBLE
            //7은 TimeLintView 에서 그려줄 때 만든 margin 값
            params = FrameLayout.LayoutParams(crop_time[2].first - crop_time[1].first +7 , binding.timeLineView.height)
            params.marginStart = ScreenSizeUtil(this).widthPixels/2 + crop_time[1].first
            binding.border.layoutParams = params
            binding.border.visibility = View.VISIBLE
        }
        else if(startX > crop_time[2].first){
            binding.border.visibility = View.INVISIBLE
            params = FrameLayout.LayoutParams(crop_time[3].first - crop_time[2].first +14 ,  binding.timeLineView.height)
            params.marginStart = ScreenSizeUtil(this).widthPixels/2 + crop_time[2].first
            binding.border.layoutParams = params
            binding.border.visibility = View.VISIBLE
        }
        else if (startX >= 0 && startX < crop_time[1].first) {
            binding.border.visibility = View.INVISIBLE
            //initialBordering()
        }
        else{
            binding.border.visibility = View.INVISIBLE
        }

    }


    private fun onVideoCompleted() {
        binding.videoLoader.seekTo(mStartPosition.toInt())
    }

    private fun updateVideoProgress(time: Float) {
        if (binding.videoLoader == null) return
        binding.handlerTop.visibility = View.VISIBLE
        if (time >= mEndPosition) {
            //mMessageHandler.removeMessages(SHOW_PROGRESS)
            binding.videoLoader.pause()
            binding.iconVideoPlay.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
    }
//


    fun removeMode(){
        binding.videoFrameView.setBackgroundResource(R.color.background_space)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, mSrc)
        var bitmap = mediaMetadataRetriever.getFrameAtTime(touch_time.get().toLong() * 1000 , MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        binding.videoFrameView.setBackgroundImage(bitmap, BackgroundType.BITMAP, BackgroundScale.CENTER_INSIDE)
        Toast.makeText(this,"지울 곳을 칠해주세요",Toast.LENGTH_LONG).show()

        removeColor.set(arrayOf(false,false,false,false))
        removeColor.set(arrayOf(true,false,false,false))

        hideVideoView()
    }

    private fun showVideoView(){
//        if(video_loader.visibility == View.INVISIBLE) {
        binding.videoLoader.visibility = View.VISIBLE
        binding.videoFrameView.visibility = View.INVISIBLE
//            clearDraw()
//        }
    }

    private fun hideVideoView(){
        if(binding.videoLoader.visibility == View.VISIBLE) {
            binding.videoLoader.visibility = View.INVISIBLE
            binding.videoFrameView.visibility = View.VISIBLE
        }
    }




    fun setVideoInformationVisibility(visible: Boolean): TrimmerActivity {
        // timeFrame.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }


    fun setDestinationPath(path: String): TrimmerActivity {
        destinationPath = path
        return this
    }

    private fun setupDatabinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer)
        binding.trimmer = this@TrimmerActivity
        //presenter = TrimmerPresenter(this)
    }


    fun back(v: View){
        onCancelClicked()
    }

    fun save(v: View) {
        onSaveClicked()
        presenter.getResult(progressDialog, this, uri = mSrc )
        progressDialog.dismiss()
    }

    fun onCancelClicked() {
        binding.videoLoader.stopPlayback()
        cancelAction()
    }

    fun onSaveClicked() {
        onTrimStarted()
        binding.iconVideoPlay.visibility = View.VISIBLE
        binding.videoLoader.pause()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, mSrc)
        val metaDataKeyDuration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

        val file = File(mSrc.path ?: "")

        if (mTimeVideo < MIN_TIME_FRAME) {
            if (metaDataKeyDuration - mEndPosition > MIN_TIME_FRAME - mTimeVideo) mEndPosition += MIN_TIME_FRAME - mTimeVideo
            else if (mStartPosition > MIN_TIME_FRAME - mTimeVideo) mStartPosition -= MIN_TIME_FRAME - mTimeVideo
        }

        val root = File(destinationPath)
        root.mkdirs()
        val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
        val outPutPath = RealPathUtil.realPathFromUriApi19(this, outputFileUri)
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
        VideoOptions(this).trimVideo(TrimVideoUtils.stringForTime(mStartPosition), TrimVideoUtils.stringForTime(mEndPosition), file.path, outPutPath, outputFileUri, this)
    }


    override fun onTrimStarted() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Started Trimming", Toast.LENGTH_SHORT).show()
            progressDialog.show()
        }
    }

//    override fun getResult(uri: Uri) {
//        RunOnUiThread(this).safely {
//            Toast.makeText(this, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
//            val mediaMetadataRetriever = MediaMetadataRetriever()
//            mediaMetadataRetriever.setDataSource(this, uri)
//            val duration =
//                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//                    .toLong()
//            val width =
//                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
//                    .toLong()
//            val height =
//                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
//                    .toLong()
//            val values = ContentValues()
//            values.put(MediaStore.Video.Media.DATA, uri.path)
//            values.put(MediaStore.Video.VideoColumns.DURATION, duration)
//            values.put(MediaStore.Video.VideoColumns.WIDTH, width)
//            values.put(MediaStore.Video.VideoColumns.HEIGHT, height)
//            val id = ContentUris.parseId(
//                contentResolver.insert(
//                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                    values
//                )
//            )
//            Log.e("VIDEO ID", id.toString())
//        }
//        progressDialog.dismiss()
//
//    }
    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    override fun cancelAction() {
        RunOnUiThread(this).safely {
            this.destroy()
            finish()
        }
    }

    override fun onError(message: String) {
        Timber.e(message)
    }

    override fun onVideoPrepared() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}
