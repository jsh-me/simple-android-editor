package kr.co.jsh.feature.storage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kr.co.domain.utils.loadUrl
import kr.co.jsh.R
import kr.co.jsh.databinding.ItemStorageDetailListBinding

class StorageAdapter (
    private val click:(Int, String, String) -> Unit,
    private var list: ArrayList<List<String>>
)
    : RecyclerView.Adapter<StorageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemStorageDetailListBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.resultStorageView, mBinding.resultName, mBinding.videoIcon)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_loader)
            .error(R.drawable.ic_error)

        if(list[position][2] =="video"){
            holder.videoIcon.visibility = View.VISIBLE
            holder.resultViewThumbnail.loadUrl(list[position][0], requestOptions)
        } else {
            holder.videoIcon.visibility = View.INVISIBLE
            holder.resultViewThumbnail.loadUrl(list[position][0], requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE))
        }
       // holder.resultViewThumbnail.loadUrl(list[position][0], requestOptions)
        holder.resultViewName.text = list[position][1]
        //holder.videoIcon.visibility = if(list[position][2] == "video") View.VISIBLE else View.INVISIBLE
        holder.resultViewThumbnail.setOnClickListener { click(position, list[position][0], list[position][2] ) }
    }

    inner class ViewHolder(
        mBinding: ItemStorageDetailListBinding,
        val resultViewThumbnail : ImageView,
        val resultViewName : TextView,
        val videoIcon: ImageView
    ) : RecyclerView.ViewHolder(mBinding.root)
}
