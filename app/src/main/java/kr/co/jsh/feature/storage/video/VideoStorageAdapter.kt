package kr.co.jsh.feature.storage.video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.jsh.databinding.ItemVideoStorageListBinding
import java.io.File

class VideoStorageAdapter (val click:(Int) -> Unit, private var resultVideoList : ArrayList<File>, private var context: Context)
    : RecyclerView.Adapter<VideoStorageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemVideoStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.resultVideoStorageView)
    }

    override fun getItemCount(): Int = resultVideoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load("http://192.168.0.188:8080/file/fileDownload.do?objectPid=6670881266423865600")
            .into(holder.resultViewThumbnail)
        holder.resultViewThumbnail.setOnClickListener { click(position) }
    }


    inner class ViewHolder(
        mBinding: ItemVideoStorageListBinding,
        val resultViewThumbnail : ImageView
    ) : RecyclerView.ViewHolder(mBinding.root)
}