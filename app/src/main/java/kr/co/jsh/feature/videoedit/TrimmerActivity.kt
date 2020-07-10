package kr.co.jsh.feature.videoedit

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.views.DrawView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.coroutines.*
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
    private lateinit var dispatcher : PausableDispatcher
    private var trimVideoTimeList: ArrayList<Pair<Long, Long>> = ArrayList()
    private var mBitmaps: ArrayList<ArrayList<Bitmap>> = ArrayList()
    private var mScreenSize = ObservableField<Int>()
    private var mDuration : Long = 0L
    private var userVideoTrimTime: MutableLiveData<Long> = MutableLiveData(0)
    private val frameSecToSendServer = ArrayList<Long> ()
    private var realVideoSize = ArrayList<Int>()
    private var videoOption = ""
    private var drawMaskCheck = false
    private var destinationPath: String = ""
    private var setPlayFlag = false
    private var timeListFlag = ObservableField<Boolean>(true)

    var canDrawUndo : ObservableField<Boolean> = ObservableField(false)
    var canDrawRedo : ObservableField<Boolean> = ObservableField(false)

    private var lastTrimmedPosition = 0L
    private var trimUndoCount = 0
    private var stackTrimList : ArrayList<Pair<Long, Long>> = ArrayList()
    private var dynamicViewSpace: ArrayList<View> = ArrayList()
    private var stackDynamicViewSpace: ArrayList<View> = ArrayList()

    val changeTextColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false,false,false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
        prepareVideo()
        setupDrawView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_edit)
        binding.trimmer = this@TrimmerActivity
    }

    private fun initView(){
        presenter = TrimmerPresenter(this, get(), get(), get())
        mScreenSize = ObservableField(ScreenSizeUtil(this).widthPixels/2)
        dispatcher = PausableDispatcher(Handler(Looper.getMainLooper()))
        binding.handlerTop.progress =  binding.handlerTop.max / 2
        binding.handlerTop.isEnabled = false
    }

    private fun prepareVideo(){
        binding.videoLoader.hideController()
        binding.videoLoader.useController = false

        setupPermissions(this) {
            val extraIntent = intent
            presenter.preparePath(extraIntent)
        }
    }

    private fun setupDrawView(){
        binding.videoFrameDrawView.setOnDrawViewListener(object : DrawView.OnDrawViewListener {
            override fun onEndDrawing() { canDrawUndoRedo() }
            override fun onStartDrawing() { canDrawUndoRedo() }
            override fun onClearDrawing() { canDrawUndoRedo() }
            override fun onAllMovesPainted() { canDrawUndoRedo() }
            override fun onRequestText() {}
        })
    }

    override fun setPlayer(player: SimpleExoPlayer) {
        binding.videoLoader.player = player
        presenter.getVideoListener() //listener
        binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        binding.videoStartTimeTv.text = String.format("%s", TrimVideoUtils.stringForTime(player.currentPosition.toFloat()))
    }

    override fun setVideoDuration(duration: Long) {
        mDuration = duration
        binding.videoEndTimeTv.text =  String.format("%s", TrimVideoUtils.stringForTime(mDuration.toFloat()))
        initTimeList(timeListFlag.get()!!)
        setListener()
    }

    private fun initTimeList(b: Boolean){
       if(b) {
           trimVideoTimeList.apply {
               clear()
               add(Pair(0, 0))
               add(Pair(binding.videoEditRecycler.width - ScreenSizeUtil(applicationContext).widthPixels.toLong(), mDuration))
           }
               timeListFlag.set(false)
       }
    }

    fun playVideo() {
        changeTextColor.set(arrayOf(false, false, false, false, false))
        showVideoView()
        binding.iconVideoPauseBtn.visibility = View.VISIBLE
        binding.iconVideoPlayBtn.visibility = View.INVISIBLE
        presenter.isVideoPlay(true)
        startThread()
        dispatcher.resume()
    }

    fun pauseVideo(){
        changeTextColor.set(arrayOf(false, false, false, false, false))
        binding.iconVideoPauseBtn.visibility = View.INVISIBLE
        binding.iconVideoPlayBtn.visibility = View.VISIBLE
        dispatcher.pause()
        presenter.isVideoPlay(false)
    }

    override fun onVideoFinished() {
        pauseVideo()
    }

    override fun setVideoPlayFlag(whenReady: Boolean) {
        setPlayFlag = whenReady
    }

    //지울 객체 그리기
    fun removeMode(){
        drawMaskCheck = true
        if(trimVideoTimeList.size <= 2) {
            presenter.uploadFile(mSrc.toString())
        }
        else {
            presenter.trimVideo(destinationPath, this, mSrc, frameSecToSendServer[0].toInt(), frameSecToSendServer[1].toInt())
        }
            binding.videoFrameDrawView.setBackgroundResource(R.color.grey1)
            presenter.getFrameBitmap(userVideoTrimTime.value!!)

            this.toastShort("지울 곳을 칠해주세요")
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(true, false, false, false, false))

            hideVideoView()
    }

    @SuppressLint("CheckResult")
    override fun setDrawBitmap(bitmap: Bitmap) {
        val playerHeight = binding.videoLoader.subtitleView?.measuredHeight!!
        val playerWidth = binding.videoLoader.subtitleView?.measuredWidth!!
        realVideoSize.add(bitmap.width)
        realVideoSize.add(bitmap.height)

       binding.videoFrameDrawView.layoutParams =
           ConstraintLayout.LayoutParams(playerWidth, playerHeight).apply {
            leftToLeft = R.id.video_edit_main_layout
            rightToRight = R.id.video_edit_main_layout
            bottomToTop = R.id.icon_video_play_btn
            topToBottom = R.id.video_edit_back_btn
        }
        binding.videoFrameDrawView.setBackgroundImage(bitmap, BackgroundType.BITMAP, BackgroundScale.FIT_START)
    }

    private fun hideVideoView(){
        binding.videoLoader.visibility = View.INVISIBLE
        binding.videoFrameDrawView.visibility = View.VISIBLE
    }

    private fun showVideoView(){
        binding.videoLoader.visibility = View.VISIBLE
        binding.videoFrameDrawView.visibility = View.INVISIBLE
    }

    fun resetTimeLineView(){
        if(trimVideoTimeList.size <= 2) this.toastShort("자르기를 먼저 실행하세요.")
        else {
            initTimeList(true)
            resetCropView()
            presenter.resetTrimVideoLIst()
            for(i in dynamicViewSpace.indices) {
                binding.videoEditChildFrameLayout.removeView(dynamicViewSpace[i])
            }
            //all list clear
            dynamicViewSpace.clear()
            stackDynamicViewSpace.clear()
            stackTrimList.clear()
            initTimeList(true)

            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, true, false, false, false))
        }
    }

    private fun resetCropView() {
        binding.selectedTimeLineView.visibility = View.INVISIBLE
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
                        if(trimVideoTimeList.size >=3){
                            setBorder(presenter.getVideoCurrentPosition().toLong())
                        }
                    }
                    delay(1)
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        binding.videoEditScrollView.setOnScrollChangeListener { _: View, scrollX: Int, _: Int, oldScrollX: Int, _: Int ->
            if (scrollX != oldScrollX && !setPlayFlag) {
                binding.videoLoader.visibility = View.VISIBLE
                binding.videoFrameDrawView.visibility = View.INVISIBLE
                userVideoTrimTime.value= (mDuration * binding.videoEditScrollView.scrollX) / ((binding.videoEditRecycler.width) - ScreenSizeUtil(applicationContext).widthPixels)
                presenter.setVideoSeekTo(userVideoTrimTime.value!!)
                binding.videoStartTimeTv.text = String.format("%s", TrimVideoUtils.stringForTime(userVideoTrimTime.value!!.toFloat()))

                if(trimVideoTimeList.size >=3){
                    setBorder(userVideoTrimTime.value!!)
                }
            }
        }
    }

    private fun setBorder(frameSec: Long){
        for (i in 0 .. trimVideoTimeList.size-2){
            if(frameSec <= trimVideoTimeList[i+1].second && frameSec >= trimVideoTimeList[i].second) selectedVideoFrames(i, i+1)
        }
    }

    private fun selectedVideoFrames(start: Int, end: Int){
        val params = FrameLayout.LayoutParams(trimVideoTimeList[end].first.toInt() - trimVideoTimeList[start].first.toInt() -7 , binding.videoEditRecycler.height-10)
            .apply{ marginStart = ScreenSizeUtil(applicationContext).widthPixels/2 + trimVideoTimeList[start].first.toInt() + 7}
        binding.selectedTimeLineView.layoutParams = params
        binding.selectedTimeLineView.visibility = View.VISIBLE

        frameSecToSendServer.apply{
            clear()
            add(trimVideoTimeList[start].second)
            add(trimVideoTimeList[end].second)
        }
        Timber.d("${frameSecToSendServer[0]} and ${frameSecToSendServer[1]}")

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
        presenter.setCuttingVideo(this, trimVideoTimeList, binding.videoEditRecycler)
    }

    override fun setGreyLine(list: ArrayList<Pair<Long, Long>>, trimmedPosition: Long) {
        lastTrimmedPosition = trimmedPosition
        trimVideoTimeList = list
        val param = FrameLayout.LayoutParams(7, FrameLayout.LayoutParams.MATCH_PARENT)
            .apply {
                setMargins(trimmedPosition.toInt() + ScreenSizeUtil(applicationContext).widthPixels/2, 0, 0, 0)
            }
        val mView = View(this)
        mView.layoutParams = param
        mView.setBackgroundColor(resources.getColor(R.color.grey2, null))
        binding.videoEditChildFrameLayout.addView(mView)
        dynamicViewSpace.add(mView)
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

    // stackTrimList , trimVideoTimeList(sorted) , originalVideoTrimList(not sorted) -> stack 쌓듯이 구현
    fun videoEditUndoBtn(){
        //------ trim 기록 제어
        stackTrimList.add(presenter.getIndexOfTrimVideoList(trimUndoCount))
        trimVideoTimeList.remove(presenter.getIndexOfTrimVideoList(trimUndoCount))
        trimUndoCount++
        //---- trim line 제어
        binding.videoEditChildFrameLayout.removeView(dynamicViewSpace.last())
        stackDynamicViewSpace.add(dynamicViewSpace.last())
        dynamicViewSpace.removeAt(dynamicViewSpace.lastIndex)
    }

    fun videoEditRedoBtn(){
        trimVideoTimeList.add(stackTrimList.last())
        stackTrimList.removeAt(stackTrimList.lastIndex)
        trimVideoTimeList.sortBy{ it.first }

        binding.videoEditChildFrameLayout.addView(stackDynamicViewSpace.last())
        dynamicViewSpace.add(stackDynamicViewSpace.last())
        stackDynamicViewSpace.removeAt(stackDynamicViewSpace.lastIndex)
    }

    fun sendRemoveVideoInfoToServer(){
        videoOption = Consts.DEL_OBJ
        if(drawMaskCheck) {
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
        if(trimVideoTimeList.size <= 2) {
            //this.toastShort("구간을 먼저 잘라주세요")
            presenter.uploadFile(mSrc.toString())
        }
        else {
            job = CoroutineScope(Dispatchers.Main).launch {
                startAnimation()
                CoroutineScope(Dispatchers.Default).async {
                    presenter.trimVideo(destinationPath, applicationContext, mSrc, frameSecToSendServer[0].toInt(), frameSecToSendServer[1].toInt())
                }.await()
            }
        }
            changeTextColor.set(arrayOf(false, false, false, false, false))
            changeTextColor.set(arrayOf(false, false, false, false, true))
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
            canDrawUndoRedo()
    }

    fun redoBtn(){
            binding.videoFrameDrawView.redo()
            canDrawUndoRedo()
    }

    private fun canDrawUndoRedo(){
        if(binding.videoFrameDrawView.canUndo()) {
            canDrawUndo.set(true)
        } else {
            canDrawUndo.set(false)
        }

        if(binding.videoFrameDrawView.canRedo()) {
            canDrawRedo.set(true)
        }
        else {
            canDrawRedo.set(false)
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

    override fun onUserLeaveHint() {
        binding.videoFrameDrawView.restartDrawing()
        Timber.e("onUserLeaveHint")
        super.onUserLeaveHint()
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