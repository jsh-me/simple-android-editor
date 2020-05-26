package kr.co.jsh.feature.storage.video

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.jsh.databinding.ItemVideoStorageListBinding
import java.io.File
import java.net.URL

class VideoStorageAdapter (val click:(Int, String) -> Unit,
                           private var resultVideoList : ArrayList<String>,
                           private var resultVideoName: ArrayList<String>,
                           private var context: Context)
    : RecyclerView.Adapter<VideoStorageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemVideoStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.resultVideoStorageView, mBinding.resultVideoName)
    }

    override fun getItemCount(): Int = resultVideoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).asBitmap().load(resultVideoList[position])
            .into(holder.resultViewThumbnail)
        holder.resultViewName.text = resultVideoName[position]
        holder.resultViewThumbnail.setOnClickListener { click(position, resultVideoList[position]) }
    }


    inner class ViewHolder(
        mBinding: ItemVideoStorageListBinding,
        val resultViewThumbnail : ImageView,
        val resultViewName : TextView
    ) : RecyclerView.ViewHolder(mBinding.root)
}
