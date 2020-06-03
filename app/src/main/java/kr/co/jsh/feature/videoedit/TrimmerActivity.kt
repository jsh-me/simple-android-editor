package kr.co.jsh.feature.videoedit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import androidx.recyclerview.widget.LinearLayoutManager
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.views.DrawView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_video_edit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.PidClass
import kr.co.domain.utils.toastShort
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityVideoEditBinding
import kr.co.jsh.feature.fullscreen.VideoViewActivity
import kr.co.jsh.feature.sendMsg.SuccessSendMsgActivity
import kr.co.jsh.localclass.PausableDispatcher
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.*
import kr.co.jsh.utils.BitmapUtil.createBinaryMask
import kr.co.jsh.utils.BitmapUtil.cropBitmapImage
import kr.co.jsh.utils.BitmapUtil.resizeBitmapImage
import kr.co.jsh.utils.permission.setupPermissions
import kr.co.jsh.utils.videoUtil.TrimVideoUtils
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.File
import org.koin.android.ext.android.get


class TrimmerActivity : AppCompatActivity(), TrimmerContract.View {
    private lateinit var binding: ActivityVideoEditBinding
    override lateinit var presenter : TrimmerContract.Presenter
    private lateinit var job: Job
    private lateinit var mSrc: Uri
    private lateinit var trimVideoTimeList: ArrayList<Pair<Int, Int>>
    private lateinit var dispatcher : PausableDispatcher
    private lateinit var mBitmaps: ArrayList<ArrayList<Bitmap>>
    private var mScreenSize = ObservableField<Int>()
    private var userCropTouchCount = 0
    private var mDuration : Float = 0f
    private var userVideoTrimTime = ObservableFloat(0f)
    private var mStartPosition = 0f
    private var mSpecificFrameBitmap : Bitmap? = null
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    private val frameSecToSendServer = ArrayList<Int> ()
    private var realVideoSize = ArrayList<Int>()
    private var videoOption = ""
    private var drawMaskCheck = false
    private var destinationPath: String=""
    var canUndo : ObservableField<Boolean> = ObservableField(false)
    var canRedo : ObservableField<Boolean> = ObservableField(false)
    val changeTextColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false,false,false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
        setupDrawView()
    }

    private fun initView(){
        presenter = TrimmerPresenter(this, get(), get(), get())
        mScreenSize = ObservableField(ScreenSizeUtil(this).widthPixels/2)
        dispatcher = PausableDispatcher(Handler(Looper.getMainLooper()))
        mBitmaps = ArrayList()
        setupPermissions(this) {
            val extraIntent = intent
            presenter.preparePath(extraIntent)
        }

        binding.handlerTop.progress =  binding.handlerTop.max / 2
        binding.handlerTop.isEnabled = false

        trimVideoTimeList = arrayListOf() //initialize
        trimVideoTimeList.add(Pair(0, 0))//1

        mDuration = binding.videoLoader.duration.toFloat()
        binding.videoLoader.setOnPreparedListener {
                mp -> onVideoPrepared(mp) }

        binding.videoEditChildFrameLayout.clipToOutline = true
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_edit)
        binding.trimmer = this@TrimmerActivity
    }

    private fun setupDrawView(){
        binding.videoFrameDrawView.setOnDrawViewListener(object : DrawView.OnDrawViewListener {
            override fun onEndDrawing() {
                canUndoRedo()
            }

            override fun onStartDrawing() {
                canUndoRedo()
            }

            override fun onClearDrawing() {
                canUndoRedo()
            }

            override fun onAllMovesPainted() {
                canUndoRedo()
            }

            override fun onRequestText() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }


    private fun onVideoPrepared(mp: MediaPlayer) {
        val lp = binding.videoLoader.layoutParams

        binding.handlerTop.visibility=View.VISIBLE
        binding.videoLoader.layoutParams = lp
        mDuration = binding.videoLoader.duration.toFloat()
        realVideoSize.add(mp.videoWidth)
        realVideoSize.add(mp.videoHeight)

        setTimeFrames()
        onVideoPrepared()
    }

    private fun onVideoCompleted() {
        binding.videoLoader.seekTo(mStartPosition.toInt())
    }

    fun playVideo() { //플레이 버튼을 눌렀을
        changeTextColor.set(arrayOf(false, false, false, false, false))
        onVideoPlayFinished()
        binding.iconVideoPauseBtn.visibility = View.VISIBLE
        binding.iconVideoPlayBtn.visibility = View.INVISIBLE
        binding.videoLoader.seekTo(userVideoTrimTime.get().toInt())
        binding.videoLoader.start()
        startThread()
        dispatcher.resume()
    }

    fun pauseVideo(){ //정지 버튼을 눌렀을 때
        changeTextColor.set(arrayOf(false, false, false, false, false))
        binding.iconVideoPauseBtn.visibility = View.INVISIBLE
        binding.iconVideoPlayBtn.visibility = View.VISIBLE
        binding.videoLoader.pause()
        userVideoTrimTime.set(binding.videoLoader.currentPosition.toFloat())
        binding.videoLoader.seekTo(binding.videoLoader.currentPosition)
        dispatcher.pause()
    }

    private fun onVideoPlayFinished(){
        binding.videoLoader.setOnCompletionListener{
            onVideoCompleted()
            dispatcher.cancel()
        }
    }

    //지울 객체 그리기
    fun removeMode(){
        drawMaskCheck = true
        if(userCropTouchCount < 2) {
            this.toastShort("구간을 먼저 잘라주세요")
        } else {
            binding.videoLoader.pause()
            binding.videoFrameDrawView.setBackgroundResource(R.color.grey1)
            mediaMetadataRetriever.setDataSource(this, mSrc)
            //지울 곳의 프레임위치
            mSpecificFrameBitmap = mediaMetadataRetriever.getFrameAtTime(
                userVideoTrimTime.get().toLong() * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            binding.videoFrameDrawView.setBackgroundImage(
                mSpecificFrameBitmap as Bitmap,
                BackgroundType.BITMAP,
                BackgroundScale.FIT_START
            )

            this.toastShort("지울 곳을 칠해주세요")

            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(true, false, false, false, false))

            hideVideoView()
            //미리 서버에 올리기
            presenter.trimVideo(destinationPath, this, mSrc, frameSecToSendServer[0], frameSecToSendServer[1])
        }
    }

    private fun hideVideoView(){
        if(binding.videoLoader.visibility == View.VISIBLE) {
            binding.videoLoader.visibility = View.INVISIBLE
            binding.videoFrameDrawView.visibility = View.VISIBLE
        }
    }

    fun resetTimeLineView(){
        if(frameSecToSendServer.size != 2) this.toastShort("자르기를 먼저 실행하세요.")
        else {
            binding.videoLoader.pause()
            userCropTouchCount = 0
            presenter.getCropArrayList(this, trimVideoTimeList)
            binding.border1.visibility = View.INVISIBLE
            binding.border2.visibility = View.INVISIBLE

            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, true, false, false, false))
        }
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
                binding.videoStartTimeTv.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(binding.videoLoader.currentPosition.toFloat())
                )
                //시간 흐를때마다 뷰 옆으로 이동!
                binding.videoEditScrollView.scrollTo(
                    ( binding.videoLoader.currentPosition * (binding.videoEditRecycler.width - ScreenSizeUtil(
                        applicationContext
                    ).widthPixels)) /  binding.videoLoader.duration, 0
                )
            }
            delay(1)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTimeFrames() {
        binding.videoEndTimeTv.text =
            String.format("%s", TrimVideoUtils.stringForTime(mDuration))
        binding.videoStartTimeTv.text = String.format(
            "%s",
            TrimVideoUtils.stringForTime(binding.videoLoader.currentPosition.toFloat())
        )


        binding.videoEditRecycler.setOnTouchListener { _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                //편집할 영역을 선택하기
                MotionEvent.ACTION_UP, MotionEvent.ACTION_DOWN -> {
                    if (userCropTouchCount == 2) {
                        binding.videoEditRecycler.performClick()
                        setBoarderRange(motionEvent.x)
                        Log.i("touch x :", "${motionEvent.x}")
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }

        binding.videoEditScrollView.setOnScrollChangeListener { view: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollX != oldScrollX && !binding.videoLoader.isPlaying) {
                //포그라운드 처리
                CoroutineScope(Dispatchers.Main).launch {
                    binding.videoLoader.visibility = View.VISIBLE
                    binding.videoFrameDrawView.visibility = View.INVISIBLE

                    userVideoTrimTime.set(
                        (mDuration * binding.videoEditScrollView.scrollX) / ((binding.videoEditRecycler.width) - ScreenSizeUtil(applicationContext).widthPixels))
                    binding.videoLoader.seekTo(userVideoTrimTime.get().toInt())
                    binding.videoLoader.seekTo(binding.videoLoader.currentPosition)
                    binding.videoStartTimeTv.text =
                        String.format("%s", TrimVideoUtils.stringForTime(userVideoTrimTime.get()))
                }
            }
        }

    }


    //coordiX: 사용자가 터치한 좌표의 X값을 가져옴 (상대좌표)
    private fun setBoarderRange(coordiX:Float){
        //터치한 곳에서 width/2 를 빼야지 원활한 계산이 가능해짐
        val startX = coordiX - ScreenSizeUtil(this).widthPixels/2
        Log.i("startX:","$startX}")
        if(startX >= trimVideoTimeList[1].first && startX <= trimVideoTimeList[2].first) selectedVideoFrames(1,2)
        else if(startX > trimVideoTimeList[2].first)  selectedVideoFrames(2,3)
        else if (startX >= 0 && startX < trimVideoTimeList[1].first) selectedVideoFrames(0,1)
        else{
            binding.selectedTimeLineView.visibility = View.INVISIBLE
        }
    }

    private fun selectedVideoFrames(start: Int, end: Int){
        binding.selectedTimeLineView.visibility = View.INVISIBLE
        val params = FrameLayout.LayoutParams(trimVideoTimeList[end].first - trimVideoTimeList[start].first, binding.videoEditRecycler.height-10)
        params.marginStart = ScreenSizeUtil(this).widthPixels/2 + trimVideoTimeList[start].first
        binding.selectedTimeLineView.layoutParams = params
        binding.selectedTimeLineView.visibility = View.VISIBLE

        frameSecToSendServer.apply{
            clear()
            add(trimVideoTimeList[start].second)
            add(trimVideoTimeList[end].second)
        }
    }

    fun clearDraw(){
        if(binding.videoFrameDrawView.visibility == View.INVISIBLE){
            this.toastShort("지울 객체를 먼저 선택하세요.")
        }
        else {
            binding.videoFrameDrawView.restartDrawing()
            removeMode()
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, false, true, false, false))
        }
    }


    fun cuttingVideoBtn(){
        userCropTouchCount ++
        presenter.setCuttingVideo(this, userCropTouchCount, video_loader, trimVideoTimeList, binding.videoEditRecycler)
        setGreyLine()
    }

    private fun setGreyLine() {
        val param1 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)
        val param2 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)

        param1.setMargins(trimVideoTimeList[1].first + ScreenSizeUtil(this).widthPixels/2,0,0,0)

        binding.border1.apply {
            layoutParams = param1
            visibility = View.VISIBLE
        }
        param2.setMargins(trimVideoTimeList[2].first + ScreenSizeUtil(this).widthPixels/2 ,0,0,0)
        binding.border2.apply {
            layoutParams = param2
            visibility = View.VISIBLE
        }
    }

    override fun onVideoPrepared() {
        RunOnUiThread(this).safely {
           Timber.e("onVideoPrepared")
        }
    }


    private fun destroy() {
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
           Timber.i("Started Trimming")
        }
    }

    override fun setVideoPath(path: String) {
        mSrc= Uri.parse(path)
        binding.videoLoader.setVideoURI(mSrc)
        binding.videoLoader.requestFocus()
        presenter.getThumbnailList(mSrc, this)
        destinationPath = Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator
    }

    override fun setPairList(list: ArrayList<Pair<Int, Int>>) {
        trimVideoTimeList = list
    }

    override fun resetCropView() {
        binding.selectedTimeLineView.visibility = View.INVISIBLE
    }

    override fun setThumbnailListView(thumbnailList: ArrayList<Bitmap>) {
        mBitmaps.add(thumbnailList)

        binding.videoEditRecycler.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = TrimmerAdapter(mBitmaps, context)
        }
    }

    fun backBtn(){
        finish()
    }

    fun sendRemoveVideoInfoToServer(){
        videoOption = Consts.DEL_OBJ
        if(drawMaskCheck && frameSecToSendServer.isNotEmpty()) {
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, false, false, true, false))

            job = CoroutineScope(Dispatchers.Main).launch {
                startAnimation()

                CoroutineScope(Dispatchers.Default).async {
                    //Todo 서버로 자른 비디오, frametimesec, maskimg 전송
                    val maskImg = binding.videoFrameDrawView.createCapture(DrawingCapture.BITMAP)
                    maskImg?.let {
                        //자세한 코드설명은 PhotoActivity에 있음.
                        val cropBitmap = cropBitmapImage(maskImg[0] as Bitmap, binding.videoFrameDrawView.width, binding.videoFrameDrawView.height)
                        val resizeBitmap = resizeBitmapImage(cropBitmap, realVideoSize[0], realVideoSize[1])
                        val binaryMask = createBinaryMask(resizeBitmap)

                        //마스크 전송
                        presenter.uploadMaskFile(binaryMask, userVideoTrimTime.get(), applicationContext)

                    }
                }.await()
            }
            if(UserObject.loginResponse == 200) {
                job.start()
            }
            else {
                this.toastShort("로그인을 먼저 해주세요.")
                cancelJob()
            }
        }
        else {
            this.toastShort( "마스크를 먼저 그려주세요")
        }

    }

    fun sendImproveVideoInfoToServer(){
        videoOption = Consts.SUPER_RESOL
        if(userCropTouchCount < 2) {
            this.toastShort("구간을 먼저 잘라주세요")
        } else {
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, false, false, false, true))
            //미리 서버에 올리기
            job = CoroutineScope(Dispatchers.Main).launch {
                startAnimation()
                CoroutineScope(Dispatchers.Default).async {
                    presenter.trimVideo(destinationPath, applicationContext, mSrc, frameSecToSendServer[0], frameSecToSendServer[1])
                }.await()
            }
        }
    }


    fun fullScreen(){
        val intent = Intent(this, VideoViewActivity::class.java).apply{
            putExtra(Consts.VIDEO_URI, mSrc.toString())
            putExtra(Consts.VIDEO_CURRENT_POSITION, binding.videoLoader.currentPosition)
        }
        startActivityForResult(intent, 1000)
    }
    override fun getResult(uri: Uri) {
        //Todo Trim 된 결과가 여기로 넘어오고 다시 getResultUri로 들어감
        presenter.getResultUri(uri, this, videoOption)
    }

    override fun uploadSuccess(msg: String) {
       Timber.e(msg)
    }

    override fun uploadFailed(msg: String) {
        this.toastShort(msg)
        failUploadServer()
    }

    override fun cancelJob() {
        job.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1000 && resultCode == 1000 ){
            dispatcher.resume()
        }
    }

    fun undoBtn(){
            binding.videoFrameDrawView.undo()
            canUndoRedo()
    }

    fun redoBtn(){
            binding.videoFrameDrawView.redo()
            canUndoRedo()
    }

    private fun canUndoRedo(){
        if(binding.videoFrameDrawView.canUndo()) {
            canUndo.set(true)
        } else {
            canUndo.set(false)
        }

        if(binding.videoFrameDrawView.canRedo()) {
            canRedo.set(true)
        }
        else {
            canRedo.set(false)
        }
    }

    override fun startAnimation(){
        binding.loadingAnimation.playAnimation()
        binding.blockingView.visibility = View.VISIBLE
        binding.loadingAnimation.visibility = View.VISIBLE
    }

    override fun stopAnimation(){
        binding.loadingAnimation.cancelAnimation()
        binding.blockingView.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
        binding.videoFrameDrawView.restartDrawing()
        val intent = Intent(this, SuccessSendMsgActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun failUploadServer(){
        binding.loadingAnimation.cancelAnimation()
        binding.blockingView.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
    }


    override fun onStop() {
        super.onStop()
        Timber.e("onStop")
    }

    override fun onResume() {
        super.onResume()
        Timber.e("onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.e("onPause")
        dispatcher.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBitmaps.clear()
        PidClass.apply{
            videoMaskObjectPid = ""
            videoObjectPid = ""
        }
    }

}