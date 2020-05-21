package kr.co.jsh.feature.photoedit

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
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
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoEditBinding
import kr.co.jsh.dialog.DialogActivity
import kr.co.jsh.utils.*
import org.koin.android.ext.android.get
import timber.log.Timber
import java.io.File
import java.lang.IndexOutOfBoundsException


class PhotoActivity : AppCompatActivity() , PhotoContract.View {
    private lateinit var binding: ActivityPhotoEditBinding
    private lateinit var presenter: PhotoPresenter
    var texteColor: ObservableField<Array<Boolean>> = ObservableField(arrayOf(false, false, false))
    var drawCheck: ObservableField<Boolean> = ObservableField(false)
    var path = ""
    private var destinationPath = ""
    private var realImageSize = ArrayList<Int>()
    private lateinit var job: Job
    var canUndo : ObservableField<Boolean> = ObservableField(false)
    var canRedo : ObservableField<Boolean> = ObservableField(false)


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
            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH)
                presenter.setImageView(this, "file://" + path)
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
                        Timber.e("$w and $h")


                        ConstraintLayout.LayoutParams(w!!, h!!).apply {
                            leftToLeft = R.id.photo_edit_layout
                            rightToRight = R.id.photo_edit_layout
                            bottomToTop = R.id.child_layout
                            topToBottom = R.id.photoBackBtn
                            binding.drawPhotoview.layoutParams = this
                        }
                        return false
                    }
                })
                    .into(photoImageView)
            }
        }
        destinationPath =
            Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Images" + File.separator

    }

    private fun setupDrawView(){
        binding.drawPhotoview.setOnDrawViewListener(object : DrawView.OnDrawViewListener {
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

    override fun displayPhotoView(file: File) {
        binding.drawPhotoview.post {
            binding.drawPhotoview.apply {
                setBackgroundResource(R.color.background_space)
                setBackgroundImage(file, BackgroundType.FILE, BackgroundScale.FIT_START)
            }
        }
        realImageSize = FileToBitmapSize(file)
        Timber.e("${realImageSize[0]} and ${realImageSize[1]}")
    }

    fun resetButton(v: View) {
        binding.drawPhotoview.apply {
            restartDrawing()
        }
        texteColor.set(arrayOf(false, false, false))
        texteColor.set(arrayOf(true, false, false))
        initView()
    }

    //https://codechacha.com/ko/android-mediastore-insert-media-files/
    //Unknown URI: content://media/external_primary/images/media
    //오른쪽 위 아이콘

    fun savePhoto(v: View) {
        job = CoroutineScope(Dispatchers.Main).launch {
            showProgressbar()

            CoroutineScope(Dispatchers.Default).async {
                val saveImage = binding.drawPhotoview.createCapture(DrawingCapture.BITMAP)
                saveImage?.let {

                    // 불러온 resource 크기만큼 crop한다.
                    val cropBitmap = CropBitmapImage(
                        saveImage[0] as Bitmap,
                        binding.drawPhotoview.width,
                        binding.drawPhotoview.height
                    )

                    // crop된 이미지를 원본 이미지 크기로 resize 해준다.
                    val resizeBitmap =
                        ResizeBitmapImage(cropBitmap, realImageSize[0], realImageSize[1])

                    //binary mask
                    val binaryMask = CreateBinaryMask(resizeBitmap)

                    //마스크까지 그려진 그림
                    presenter.uploadFrameFile(binaryMask, applicationContext)
                    Timber.e("마스크 결과 : ${(saveImage[0] as Bitmap).width} and ${(saveImage[0] as Bitmap).height}")
                } ?: run {
                    Toast.makeText(applicationContext, "마스크를 그려주세요", Toast.LENGTH_SHORT).show()
                }
            }.await()
        }
        if (PidClass.ResponseCode == 200) job.start()
        else {
            Toast.makeText(applicationContext, "로그인을 먼저 해주세요.", Toast.LENGTH_SHORT).show()
            cancelJob()
        }
    }

    private fun showProgressbar() {
        val intent = Intent(this, DialogActivity::class.java)
        startActivity(intent)
    }

    fun drawPhotoMask(){
        texteColor.set(arrayOf(false,false,false))
        texteColor.set(arrayOf(false,true,false))
        drawCheck.set(true)
        presenter.uploadFile("file://" + path) //원본 그림

    }

    fun undoButton(){
        binding.drawPhotoview.undo()
        canUndoRedo()
    }

    fun redoButton(){
        binding.drawPhotoview.redo()
        canUndoRedo()
    }

    private fun canUndoRedo(){
        if(binding.drawPhotoview.canUndo()) {
            canUndo.set(true)
        } else {
            canUndo.set(false)
        }

        if(binding.drawPhotoview.canRedo()) {
            canRedo.set(true)
        }
        else {
            canRedo.set(false)
        }
    }

    fun backButton(){
        finish()
    }

    override fun cancelJob() {
        job.cancel()
    }

    override fun uploadSuccess(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun uploadFailed(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }
}