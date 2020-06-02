package kr.co.jsh.feature.photoedit

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.views.DrawView
import kotlinx.android.synthetic.main.activity_photo_edit.*
import kotlinx.coroutines.*
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.domain.utils.loadUrl
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoEditBinding
import kr.co.jsh.feature.sendMsg.SuccessSendMsgActivity
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.BitmapUtil.createBinaryMask
import kr.co.jsh.utils.BitmapUtil.cropBitmapImage
import kr.co.jsh.utils.BitmapUtil.fileToBitmapSize
import kr.co.jsh.utils.BitmapUtil.resizeBitmapImage
import kr.co.jsh.utils.permission.setupPermissions
import org.koin.android.ext.android.get
import timber.log.Timber
import java.io.File


class PhotoActivity : AppCompatActivity() , PhotoContract.View {
    private lateinit var binding: ActivityPhotoEditBinding
    override lateinit var presenter: PhotoContract.Presenter
    var changeTextColor: ObservableField<Array<Boolean>> = ObservableField(arrayOf(false, false, false))
    var drawCheck: ObservableField<Boolean> = ObservableField(false)
    var path = ""
    private var destinationPath = ""
    private var realImageSize = ArrayList<Int>()
    private lateinit var job: Job
    var canUndo : ObservableField<Boolean> = ObservableField(false)
    var canRedo : ObservableField<Boolean> = ObservableField(false)

   // private lateinit var testPhoto: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
        setupDrawView()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_edit)
        binding.photo = this@PhotoActivity
    }

    private fun initView() {
        val extraIntent = intent

        presenter = PhotoPresenter(this, get(), get())
        setupPermissions(this) {
            presenter.preparePath(extraIntent)

            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH) ?: ""

                Glide.with(this).asBitmap().load(path).listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val w = resource?.width
                        val h = resource?.height

                       // testPhoto = resource!!
                        Timber.e("$w and $h")


                        ConstraintLayout.LayoutParams(w!!, h!!).apply {
                            leftToLeft = R.id.photo_edit_parent_layout
                            rightToRight = R.id.photo_edit_parent_layout
                            bottomToTop = R.id.photo_edit_child_layout
                            topToBottom = R.id.photo_edit_back_btn
                            binding.photoEditDrawView.layoutParams = this
                        }
                        return false
                    }
                })
                    .into(photo_edit_iv)
            }
        }
        destinationPath =
            Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Images" + File.separator

    }

    private fun setupDrawView(){
        binding.photoEditDrawView.setOnDrawViewListener(object : DrawView.OnDrawViewListener {
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

    override fun setPhotoView(file: File) {
        binding.photoEditDrawView.post {
            binding.photoEditDrawView.apply {
                setBackgroundResource(R.color.grey1)
                setBackgroundImage(file, BackgroundType.FILE, BackgroundScale.FIT_START)
            }
        }
        realImageSize = fileToBitmapSize(file)
        Timber.e("${realImageSize[0]} and ${realImageSize[1]}")
    }

    fun resetBtn() {
        binding.photoEditDrawView.apply {
            restartDrawing()
        }
        changeTextColor.set(arrayOf(false, false, false))
        changeTextColor.set(arrayOf(true, false, false))
        initView()
    }

    //https://codechacha.com/ko/android-mediastore-insert-media-files/
    //Unknown URI: content://media/external_primary/images/media
    //오른쪽 위 아이콘

    fun saveBtn() {
        job = CoroutineScope(Dispatchers.Main).launch {
            startAnimation()

            CoroutineScope(Dispatchers.Default).async {
                val saveImage = binding.photoEditDrawView.createCapture(DrawingCapture.BITMAP)
                saveImage?.let {
                    // 불러온 resource 크기만큼 crop한다.
                    val cropBitmap = cropBitmapImage(saveImage[0] as Bitmap, binding.photoEditDrawView.width, binding.photoEditDrawView.height)
                    // crop된 이미지를 원본 이미지 크기로 resize 해준다.
                    val resizeBitmap = resizeBitmapImage(cropBitmap, realImageSize[0], realImageSize[1])
                    //binary mask
                    val binaryMask = createBinaryMask(resizeBitmap)
                    //마스크까지 그려진 그림
                    presenter.uploadFrameFile(binaryMask, applicationContext)
                    Timber.e("마스크 결과 : ${(saveImage[0] as Bitmap).width} and ${(saveImage[0] as Bitmap).height}")
                } ?: run {
                    Toast.makeText(applicationContext, "마스크를 그려주세요", Toast.LENGTH_SHORT).show()
                }
            }.await()
        }
        if (UserObject.loginResponse == 200) job.start()
        else {
           // Toast.makeText(applicationContext, "로그인을 먼저 해주세요.", Toast.LENGTH_SHORT).show()
            cancelJob()
        }
    }

    fun drawPhotoMask(){
        changeTextColor.set(arrayOf(false,false,false))
        changeTextColor.set(arrayOf(false,true,false))
        drawCheck.set(true)
        presenter.uploadFile("file://" + path) //원본 그림

    }

    fun undoBtn(){
        binding.photoEditDrawView.undo()
        canUndoRedo()
    }

    fun redoBtn(){
        binding.photoEditDrawView.redo()
        canUndoRedo()
    }

    private fun canUndoRedo(){
        if(binding.photoEditDrawView.canUndo()) {
            canUndo.set(true)
        } else {
            canUndo.set(false)
        }

        if(binding.photoEditDrawView.canRedo()) {
            canRedo.set(true)
        }
        else {
            canRedo.set(false)
        }
    }

    fun backBtn(){
        finish()
    }

    override fun cancelJob() {
        job.cancel()
    }

    override fun uploadSuccess(msg: String) {
       // Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun uploadFailed(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
        failUploadServer()
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
         binding.photoEditDrawView.restartDrawing()
         val intent = Intent(this, SuccessSendMsgActivity::class.java)
         startActivity(intent)
         finish()
    }

    private fun failUploadServer(){
        binding.loadingAnimation.cancelAnimation()
        binding.blockingView.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
    }

    override fun onError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelAction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    //-----------test code-------------//
//    fun saveTEST(){
//        val bitmap = testPhoto
//        val displayName = "${System.currentTimeMillis()}.jpg"
//        val mimeType = "image/jpeg"
//        val compressFormat = Bitmap.CompressFormat.JPEG
//        ScopeStorageFileUtil.addPhotoAlbum(bitmap, displayName, mimeType, compressFormat, this)
//        Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
//    }
}