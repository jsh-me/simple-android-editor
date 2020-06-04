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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import androidx.databinding.adapters.ViewGroupBindingAdapter.setListener
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.views.DrawView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo_edit.*
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
    private lateinit var trimVideoTimeList:   ArrayList<Pair<Long, Long>>
    private lateinit var dispatcher : PausableDispatcher
    private var mBitmaps: ArrayList<ArrayList<Bitmap>> = ArrayList()
    private var mScreenSize = ObservableField<Int>()
    private var userCropTouchCount = 0
    private var mDuration : Long = 0L
    private var userVideoTrimTime: MutableLiveData<Long> = MutableLiveData()
    private val frameSecToSendServer = ArrayList<Long> ()
    private var realVideoSize = ArrayList<Int>()
    private var videoOption = ""
    private var drawMaskCheck = false
    private var destinationPath: String=""
    private var setPlayFlag = false
    var canUndo : ObservableField<Boolean> = ObservableField(false)
    var canRedo : ObservableField<Boolean> = ObservableField(false)
    val changeTextColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false,false,false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
        prepareVideo()
        setupDrawView()
    }

    private fun initView(){
        presenter = TrimmerPresenter(this, get(), get(), get())
        mScreenSize = ObservableField(ScreenSizeUtil(this).widthPixels/2)
        dispatcher = PausableDispatcher(Handler(Looper.getMainLooper()))
        binding.handlerTop.progress =  binding.handlerTop.max / 2
        binding.handlerTop.isEnabled = false
        binding.videoLoader.resizeMode= AspectRatioFrameLayout.RESIZE_MODE_FIT
        binding.videoLoader.hideController()
        binding.videoLoader.useController = false
        trimVideoTimeList = arrayListOf() //initialize
        trimVideoTimeList.add(Pair(0, 0))//1
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_edit)
        binding.trimmer = this@TrimmerActivity
    }

    private fun prepareVideo(){
        setupPermissions(this) {
            val extraIntent = intent
            presenter.preparePath(extraIntent)
        }
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

    override fun setPlayer(player: SimpleExoPlayer) {
        binding.videoLoader.player = player
        presenter.getVideoDuration() //listener
        binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        binding.videoStartTimeTv.text = String.format("%s", TrimVideoUtils.stringForTime(player.currentPosition.toFloat()))
    }

    override fun setVideoDuration(duration: Long) {
        mDuration = duration
        binding.videoEndTimeTv.text =  String.format("%s", TrimVideoUtils.stringForTime(mDuration.toFloat()))
        setListener()
    }

    fun playVideo() { //플레이 버튼을 눌렀을
        changeTextColor.set(arrayOf(false, false, false, false, false))
        binding.iconVideoPauseBtn.visibility = View.VISIBLE
        binding.iconVideoPlayBtn.visibility = View.INVISIBLE
        presenter.isVideoPlay(true)
        startThread()
        dispatcher.resume()
    }

    fun pauseVideo(){ //정지 버튼을 눌렀을 때
        changeTextColor.set(arrayOf(false, false, false, false, false))
        binding.iconVideoPauseBtn.visibility = View.INVISIBLE
        binding.iconVideoPlayBtn.visibility = View.VISIBLE
        dispatcher.pause()
        presenter.isVideoPlay(false)
    }

    override fun setVideoPlayFlag(whenReady: Boolean) {
        setPlayFlag = whenReady
    }

    //지울 객체 그리기
    fun removeMode(){
        drawMaskCheck = true
        if(userCropTouchCount < 2) {
            this.toastShort("구간을 먼저 잘라주세요")
        } else {
            binding.videoFrameDrawView.setBackgroundResource(R.color.grey1)
            presenter.getFrameBitmap(userVideoTrimTime.value!!)

            this.toastShort("지울 곳을 칠해주세요")
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(true, false, false, false, false))

            hideVideoView()
            //미리 서버에 올리기
            presenter.trimVideo(destinationPath, this, mSrc, frameSecToSendServer[0].toInt(), frameSecToSendServer[1].toInt())

            //Todo: https://pooheaven81.tistory.com/137
        }
    }

    @SuppressLint("CheckResult")
    override fun setDrawBitmap(bitmap: Bitmap) {
        val playerHeight = binding.videoLoader.subtitleView?.measuredHeight!!
        val playerWidth = binding.videoLoader.subtitleView?.measuredWidth!!
        realVideoSize.add(bitmap.width)
        realVideoSize.add(bitmap.height)

        ConstraintLayout.LayoutParams(playerWidth, playerHeight).apply {
            leftToLeft = R.id.video_edit_main_layout
            rightToRight = R.id.video_edit_main_layout
            bottomToTop = R.id.icon_video_play_btn
            topToBottom = R.id.video_edit_back_btn
            binding.videoFrameDrawView.layoutParams = this
        }

        binding.videoFrameDrawView.setBackgroundImage(
            bitmap,
            BackgroundType.BITMAP,
            BackgroundScale.FIT_START
        )
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
           // binding.videoLoader.pause()
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
                while (setPlayFlag) {
                    applicationContext.runOnUiThread {
                        binding.videoStartTimeTv.text = String.format(
                            "%s",
                            TrimVideoUtils.stringForTime(presenter.getVideoCurrentPosition())
                        )
                        userVideoTrimTime.value = (( presenter.getVideoCurrentPosition()*(binding.videoEditRecycler.width
                                - ScreenSizeUtil(applicationContext).widthPixels)) /  mDuration).toLong()
                        binding.videoEditScrollView.scrollTo(userVideoTrimTime.value!!.toInt(), 0)
                    }
                    delay(1)
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
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

        binding.videoEditScrollView.setOnScrollChangeListener { view: View, scrollX: Int, _: Int, oldScrollX: Int, _: Int ->
            if (scrollX != oldScrollX && !setPlayFlag) {
                binding.videoLoader.visibility = View.VISIBLE
                binding.videoFrameDrawView.visibility = View.INVISIBLE
                userVideoTrimTime.value= (mDuration * binding.videoEditScrollView.scrollX) / ((binding.videoEditRecycler.width) - ScreenSizeUtil(applicationContext).widthPixels)
                presenter.setVideoSeekTo(userVideoTrimTime.value!!)
                binding.videoStartTimeTv.text = String.format("%s", TrimVideoUtils.stringForTime(userVideoTrimTime.value!!.toFloat()))
            }
        }
    }


    //coordiX: 사용자가 터치한 좌표의 X값을 가져옴 (상대좌표)
    private fun setBoarderRange(coordiX: Float){
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
        val params = FrameLayout.LayoutParams(trimVideoTimeList[end].first.toInt() - trimVideoTimeList[start].first.toInt(), binding.videoEditRecycler.height-10)
        params.marginStart = ScreenSizeUtil(this).widthPixels/2 + trimVideoTimeList[start].first.toInt()
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
        presenter.setCuttingVideo(this, userCropTouchCount, trimVideoTimeList, binding.videoEditRecycler)
        setGreyLine()
    }

    private fun setGreyLine() {
        val param1 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)
        val param2 = FrameLayout.LayoutParams(7,FrameLayout.LayoutParams.MATCH_PARENT)

        param1.setMargins(trimVideoTimeList[1].first.toInt() + ScreenSizeUtil(this).widthPixels/2,0,0,0)

        binding.border1.apply {
            layoutParams = param1
            visibility = View.VISIBLE
        }
        param2.setMargins(trimVideoTimeList[2].first.toInt() + ScreenSizeUtil(this).widthPixels/2 ,0,0,0)
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
        presenter.getThumbnailList(mSrc, this)
        destinationPath = Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator
    }

    override fun setPairList(list:  ArrayList<Pair<Long, Long>>) {
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
        presenter.releasePlayer()
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
                        val cropBitmap = cropBitmapImage(maskImg[0] as Bitmap, binding.videoFrameDrawView.width, binding.videoFrameDrawView.height)
                        val resizeBitmap = resizeBitmapImage(cropBitmap, realVideoSize[0], realVideoSize[1])
                        val binaryMask = createBinaryMask(resizeBitmap)

                        //마스크 전송
                        presenter.uploadMaskFile(binaryMask, userVideoTrimTime.value!!.toFloat(), applicationContext)

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
                    presenter.trimVideo(destinationPath, applicationContext, mSrc, frameSecToSendServer[0].toInt(), frameSecToSendServer[1].toInt())
                }.await()
            }
        }
    }


    fun fullScreen(){
        val intent = Intent(this, VideoViewActivity::class.java).apply{
            putExtra(Consts.VIDEO_URI, mSrc.toString())
            putExtra(Consts.VIDEO_CURRENT_POSITION, presenter.getVideoCurrentPosition())
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
        presenter.releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        presenter.initPlayer(mSrc, this)
    }

    override fun onRestart() {
        super.onRestart()
        presenter.initPlayer(mSrc, this)

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