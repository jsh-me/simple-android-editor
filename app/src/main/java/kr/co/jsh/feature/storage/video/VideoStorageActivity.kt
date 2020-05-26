package kr.co.jsh.feature.storage.video

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityVideoStorageBinding
import kr.co.jsh.feature.storage.detailPhoto.PhotoDetailActivity
import kr.co.jsh.feature.storage.detailVideo.VideoDetailActivity
import org.koin.android.ext.android.get
import java.io.File

class VideoStorageActivity : AppCompatActivity(), VideoStorageContract.View {
    private lateinit var binding : ActivityVideoStorageBinding
    lateinit var presenter : VideoStoragePresenter

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
        presenter.getAllVideoResultFile()
    }

    override fun setAllVideoResultView(url: ArrayList<String>, name: ArrayList<String>) {
        if(url.size == 0){
            binding.noResultText.visibility = View.VISIBLE
        } else {
            binding.videoStorageRecycler.apply {
                layoutManager = GridLayoutManager(this@VideoStorageActivity, 2)
                adapter = VideoStorageAdapter(click(), url, name, context)
            }
        }
    }

    private fun click() = { _: Int, url: String ->
        val intent = Intent(this, VideoDetailActivity::class.java).apply{
            putExtra(Consts.DETAIL_VIDEO, url)
        }
        startActivity(intent)
    }

    fun backButton(){
        finish()
    }
}