package kr.co.jsh.feature.storage.video

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityVideoStorageBinding
import kr.co.jsh.feature.storage.detailPhoto.PhotoDetailActivity
import org.koin.android.ext.android.get
import java.io.File

class VideoStorageActivity : AppCompatActivity(), VideoStorageContract.View {
    private lateinit var binding : ActivityVideoStorageBinding
    lateinit var presenter : VideoStoragePresenter
    var videoResult = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initPresenter()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_storage)
        binding.videoStorage = this@VideoStorageActivity
    }

    private fun initPresenter(){
        presenter = VideoStoragePresenter(this, get())
        repeat(PidClass.topVideoObjectPid.size) {
            presenter.getVideoResultFile(PidClass.topVideoObjectPid[it])
        }
    }

    override fun setVideoResultView(url: String) {
        videoResult.add(url)

        if (videoResult.size == PidClass.topVideoObjectPid.size) {
            initRecyclerView()
        }
    }

    private fun initRecyclerView() {
            binding.videoStorageRecycler.apply {
                layoutManager = GridLayoutManager(this@VideoStorageActivity, 3)
                adapter = VideoStorageAdapter(click(), videoResult, context)
            }
        }


    private fun click() = { id: Int, url: String ->
        val intent = Intent(this, PhotoDetailActivity::class.java).apply{
            putExtra(Consts.DETAIL_PHOTO, url)
        }
        startActivity(intent)
    }

    fun backButton(){
        finish()
    }
}