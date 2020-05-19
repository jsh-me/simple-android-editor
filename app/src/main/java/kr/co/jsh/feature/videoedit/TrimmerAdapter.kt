package kr.co.jsh.feature.videoedit

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.jsh.R
import kr.co.jsh.customview.TimeLineView
import kr.co.jsh.databinding.ItemPhotoViewBinding

class TrimmerAdapter ( private var thumbnaillist : ArrayList<ArrayList<Bitmap>>, private var context: Context)
    : RecyclerView.Adapter<TrimmerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemPhotoViewBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.itemVideo)
    }

    override fun getItemCount(): Int = thumbnaillist.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.drawView(thumbnaillist[position])
    }


    inner class ViewHolder(
    mBinding: ItemPhotoViewBinding,
    val image : TimeLineView
    ) : RecyclerView.ViewHolder(mBinding.root)
}