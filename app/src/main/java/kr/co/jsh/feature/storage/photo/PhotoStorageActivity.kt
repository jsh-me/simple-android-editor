package kr.co.jsh.feature.storage.photo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_photo_storage.*
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoStorageBinding
import java.net.URL
import org.koin.android.ext.android.get


class PhotoStorageActivity : AppCompatActivity(), PhotoStorageContract.View {
    lateinit var binding : ActivityPhotoStorageBinding
    lateinit var presenter : PhotoStoragePresenter
    var imageResult = ArrayList<URL>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initRecyclerView()
        initPresenter()
    }
    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_storage)
        binding.photoStorage = this@PhotoStorageActivity
    }

    private fun initRecyclerView() {
        photoStorageRecycler.apply {
            layoutManager = GridLayoutManager(this@PhotoStorageActivity, 3)
            adapter = PhotoStorageAdapter(click(), imageResult, context)
        }
    }

    private fun initPresenter(){
        presenter = PhotoStoragePresenter(this, get())
        presenter.getResultFile(PidClass.imageObjectPid) //test: string to url convert
    }

    private fun click() = { id:Int ->
    }
}