package kr.co.jsh.feature.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import kr.co.domain.utils.loadUrl
import kr.co.jsh.databinding.ItemStorageListBinding

class MainAdapter (val click:(Int, String, String) -> Unit,
                   private var list: ArrayList<List<String>>,
                   private var context: Context
)
    : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.resultStorageView, mBinding.resultVideoName, mBinding.videoIcon)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestOptions = RequestOptions()
        requestOptions.isMemoryCacheable
        holder.resultViewThumbnail.loadUrl(list[position][0], requestOptions)
        holder.resultViewName.text = list[position][1]
        holder.videoIcon.visibility = if(list[position][2].equals("video")) View.VISIBLE else View.INVISIBLE
        holder.resultViewThumbnail.setOnClickListener { click(position, list[position][0], list[position][2] ) }

    }

    inner class ViewHolder(
        mBinding: ItemStorageListBinding,
        val resultViewThumbnail: ImageView,
        val resultViewName: TextView,
        val videoIcon: ImageView
    ) : RecyclerView.ViewHolder(mBinding.root)
}