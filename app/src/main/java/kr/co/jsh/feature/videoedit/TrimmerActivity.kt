package kr.co.jsh.feature.videoedit

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import androidx.recyclerview.widget.LinearLayoutManager
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_video_edit.*
import kotlinx.coroutines.*
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityVideoEditBinding
import kr.co.jsh.localclass.PausableDispatcher
import kr.co.jsh.utils.*
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import org.koin.android.ext.android.get


class TrimmerActivity : AppCompatActivity(), TrimmerContract.View {
    private lateinit var binding: ActivityVideoEditBinding
    private lateinit var presenter : TrimmerPresenter
    private var screenSize = ObservableField<Int>()
    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null
    private var crop_count = 0
    lateinit var crop_time: ArrayList<Pair<Int, Int>>
    private var timeposition = 0
    private var mDuration : Float = 0f
    private var touch_time = ObservableFloat()
    private var mStartPosition = 0f
    private lateinit var progressDialog : VideoProgressIndeterminateDialog
    val texteColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false,false))
    private var myPickBitmap : Bitmap? = null
    val mediaMetadataRetriever = MediaMetadataRetriever()

    private val dispatcher =
        PausableDispatcher(Handler(Looper.getMainLooper()))

    private lateinit var mBitmaps: ArrayList<ArrayList<Bitmap>>

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
    }

    private fun initView(){
        presenter = TrimmerPresenter(this, get())
        screenSize = ObservableField(ScreenSizeUtil(this).widthPixels/2)
        mBitmaps = ArrayList()
        progressDialog = VideoProgressIndeterminateDialog(this, "Cropping Video. Please Wait...")

        setupPermissions(this) {
            val extraIntent = intent
            presenter.prepareVideoPath(extraIntent)
        }

        binding.handlerTop.progress =  binding.handlerTop.max / 2
        binding.handlerTop.isEnabled = false

        crop_time = arrayListOf() //initialize
        crop_time.add(Pair(0, 0))//1

        mDuration = binding.videoLoader.duration.toFloat()
        binding.videoLoader.setOnPreparedListener {
                mp -> onVideoPrepared(mp) }
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_edit)
        binding.trimmer = this@TrimmerActivity
    }


    private fun onVideoPrepared(mp: MediaPlayer) {
        val lp = binding.videoLoader.layoutParams

        binding.handlerTop.visibility=View.VISIBLE
        binding.videoLoader.layoutParams = lp
        mDuration = binding.videoLoader.duration.toFloat()

        setTimeFrames()
        onVideoPrepared()
    }

    private fun onVideoCompleted() {
        binding.videoLoader.seekTo(mStartPosition.toInt())
    }

//    private fun updateVideoProgress(time: Float) {
//        if (binding.videoLoader == null) return
//        binding.handlerTop.visibility = View.VISIBLE
//        if (time >= mDuration) {
//            //mMessageHandler.removeMessages(SHOW_PROGRESS)
//            binding.videoLoader.pause()
//            binding.iconVideoPlay.visibility = View.VISIBLE
////            mResetSeekBar = true
//            return
//        }
//    }


    fun playVideo() {
        binding.videoLoader.setOnCompletionListener{
            binding.iconVideoPlay.isSelected = false
            onVideoCompleted()
            dispatcher.cancel()
        }

        if (binding.videoLoader.isPlaying) {
            binding.iconVideoPlay.isSelected = false
            timeposition = binding.videoLoader.currentPosition
            binding.videoLoader.seekTo(timeposition)
            binding.videoLoader.pause()
            dispatcher.pause()

        } else {
            texteColor.set(arrayOf(false,false,false,false))
            binding.iconVideoPlay.isSelected = true
            binding.videoLoader.setOnPreparedListener {
               mp ->
                mp.setOnSeekCompleteListener {
                    binding.videoLoader.seekTo(timeposition)
                    }
            }
            binding.videoLoader.start()
            startThread()
            dispatcher.resume()
        }
    }

    //지울 객체 그리기
    fun removeMode(){
        if(crop_count < 2) {
            Toast.makeText(this, "구간을 먼저 잘라주세요", Toast.LENGTH_LONG).show()
        } else {
            binding.iconVideoPlay.isSelected = false
            binding.videoLoader.pause()
            binding.videoFrameView.setBackgroundResource(R.color.background_space)
//            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(this, mSrc)
            //지울 곳의 프레임위치
            myPickBitmap = mediaMetadataRetriever.getFrameAtTime(
                touch_time.get().toLong() * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            binding.videoFrameView.setBackgroundImage(
                myPickBitmap as Bitmap,
                BackgroundType.BITMAP,
                BackgroundScale.CENTER_INSIDE
            )
            Toast.makeText(this, "지울 곳을 칠해주세요", Toast.LENGTH_LONG).show()

            texteColor.set(arrayOf(false, false, false, false))
            texteColor.set(arrayOf(true, false, false, false))

            hideVideoView()
        }
    }

    private fun hideVideoView(){
        if(binding.videoLoader.visibility == View.VISIBLE) {
            binding.videoLoader.visibility = View.INVISIBLE
            binding.videoFrameView.visibility = View.VISIBLE
        }
    }

    fun resetTimeLineView(){
        binding.iconVideoPlay.isSelected = false
        binding.videoLoader.pause()
        crop_count = 0
        presenter.resetCrop(this, crop_time)
        binding.boader1.visibility = View.INVISIBLE
        binding.boader2.visibility = View.INVISIBLE

        texteColor.set(arrayOf(false,false,false,false))
        texteColor.set(arrayOf(false,false,true,false))
    }



    override fun onError(message: String) {
        Timber.e(message)
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
                    ( binding.videoLoader.currentPosition * (binding.timeLineViewRecycler.width - ScreenSizeUtil(
                        applicationContext
                    ).widthPixels)) /  binding.videoLoader.duration, 0
                )
            }
            delay(1)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTimeFrames() {
        binding.textTimeSelection.text =
            String.format("%s", TrimVideoUtils.stringForTime(mDuration))
        binding.textStartTime.text = String.format(
            "%s",
            TrimVideoUtils.stringForTime(binding.videoLoader.currentPosition.toFloat())
        )


        binding.timeLineViewRecycler.setOnTouchListener { _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                //편집할 영역을 선택하기
                MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> {
                    try {
                        if (crop_count == 2) {
                            binding.timeLineViewRecycler.performClick()
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
                        //showVideoView() //재생 버튼 누른 후 remove 하면 이상하게 동작함. 무조건 seekbar로 이동시에만 정상동작.
                        binding.videoLoader.visibility = View.VISIBLE
                        binding.videoFrameView.visibility = View.INVISIBLE

                        touch_time.set(
                            (mDuration * it) / ((binding.timeLineViewRecycler.width) - ScreenSizeUtil(
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


    //coordiX: 사용자가 터치한 좌표의 X값을 가져옴 (상대좌표)
    private fun setBoarderRange(coordiX:Float){
        //터치한 곳에서 width/2 를 빼야지 원활한 계산이 가능해짐
        var params = FrameLayout.LayoutParams(0,0)
        var startX = coordiX - ScreenSizeUtil(this).widthPixels/2
        Log.i("startX:","$startX}")
        if(startX >= crop_time[1].first && startX <= crop_time[2].first){
            binding.border.visibility = View.INVISIBLE
            //7은 TimeLintView 에서 그려줄 때 만든 margin 값
            params = FrameLayout.LayoutParams(crop_time[2].first - crop_time[1].first, binding.timeLineViewRecycler.height-10)
            params.marginStart = ScreenSizeUtil(this).widthPixels/2 + crop_time[1].first
            binding.border.layoutParams = params
            binding.border.visibility = View.VISIBLE
        }
        else if(startX > crop_time[2].first){
            binding.border.visibility = View.INVISIBLE
            params = FrameLayout.LayoutParams(crop_time[3].first - crop_time[2].first,  binding.timeLineViewRecycler.height-10)
            params.marginStart = ScreenSizeUtil(this).widthPixels/2 + crop_time[2].first
            binding.border.layoutParams = params
            binding.border.visibility = View.VISIBLE
        }
        else if (startX >= 0 && startX < crop_time[1].first) {
            binding.border.visibility = View.INVISIBLE
            params = FrameLayout.LayoutParams(crop_time[1].first - crop_time[0].first,  binding.timeLineViewRecycler.height-10)
            params.marginStart = ScreenSizeUtil(this).widthPixels/2
            binding.border.layoutParams = params
            binding.border.visibility = View.VISIBLE
        }
        else{
            binding.border.visibility = View.INVISIBLE
        }

    }

    fun clearDraw(){
        if(binding.videoFrameView.visibility == View.INVISIBLE){
            Toast.makeText(this,"지울 객체를 먼저 선택하세요.",Toast.LENGTH_LONG).show()
        }
        else {
            binding.iconVideoPlay.isSelected = false
            binding.videoFrameView.restartDrawing()
            removeMode()
            texteColor.set(arrayOf(false, false, false, false))
            texteColor.set(arrayOf(false, false, false, true))
        }
    }


    fun cropVideo(){
        crop_count ++
        presenter.crop(this, crop_count, video_loader, crop_time, binding.timeLineViewRecycler)
        greyline()
    }

//Todo UPLOAD SERVER
    fun uploadServer(){
    mediaMetadataRetriever.setDataSource(this, mSrc)
    //지울 곳의 프레임위치
    myPickBitmap = mediaMetadataRetriever.getFrameAtTime(
        touch_time.get().toLong() * 1000,
        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
    )

    myPickBitmap?.let {
        presenter.uploadFile(mSrc) //video upload
        presenter.uploadFrameFile(myPickBitmap!!, this) //specific frame
    }?:run{
        Toast.makeText(this, "마스크를 먼저 그려주세요", Toast.LENGTH_SHORT).show()
    }
}

    private fun greyline() {
        val param1 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)
        val param2 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)

        param1.setMargins(crop_time[1].first + ScreenSizeUtil(this).widthPixels/2,0,0,0)

        binding.boader1.apply {
            layoutParams = param1
            visibility = View.VISIBLE
        }
        param2.setMargins(crop_time[2].first + ScreenSizeUtil(this).widthPixels/2 ,0,0,0)
        binding.boader2.apply{
            layoutParams = param2
            visibility = View.VISIBLE
        }



    }

    override fun onVideoPrepared() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }
    }


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

    override fun onTrimStarted() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Started Trimming", Toast.LENGTH_SHORT).show()
            progressDialog.show()
        }
    }

    override fun videoPath(path: String) {
        mSrc= Uri.parse(path)
        binding.videoLoader.setVideoURI(mSrc)
        binding.videoLoader.requestFocus()
        presenter.getThumbnailList(mSrc, this)

//        setVideoInformationVisibility(true)
//                .setDestinationPath("/document/" + File.separator + "returnable" + File.separator + "Videos" + File.separator)

        //setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator)
        destinationPath = Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator
    }

    override fun setPairList(list: ArrayList<Pair<Int, Int>>) {
        crop_time = list
    }

    override fun resetCropView() {
        binding.border.visibility = View.INVISIBLE
    }

    override fun setThumbnailListView(thumbnailList: ArrayList<Bitmap>) {
        mBitmaps.add(thumbnailList)

        binding.timeLineViewRecycler.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = TrimmerAdapter(mBitmaps, context)
        }
    }

    fun saveVideo(){
        //Todo 갤러리에 저장과, 서버 업로드가 같이 될 함수 (나중에 분리)
        presenter.saveVideo(destinationPath, this, mSrc, crop_time[1].second, crop_time[2].second)

    }

    override fun getResult(uri: Uri) {
        //Todo Trim 된 결과가 여기로 넘어오고 다시 getResultUri로 들어감
        presenter.getResultUri(uri, this)
        progressDialog.dismiss()
    }

    override fun uploadSuccess(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun uploadFailed(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
      //  binding.timeLineViewRecycler.adapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        mBitmaps.clear()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBitmaps.clear()
        finish()
    }
}