package kr.co.jsh.feature.videoStorage
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import kr.co.domain.utils.loadUrl
import kr.co.jsh.databinding.ItemVideoStorageListBinding

class VideoStorageAdapter (val click:(Int, String) -> Unit,
                           private var list: ArrayList<List<String>>,
                           private var context: Context)
    : RecyclerView.Adapter<VideoStorageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = ItemVideoStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(mBinding, mBinding.resultVideoStorageView, mBinding.resultVideoName)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestOptions = RequestOptions()
        requestOptions.isMemoryCacheable
        holder.resultViewThumbnail.loadUrl(list[position][0], requestOptions)
        holder.resultViewName.text = list[position][1]
        holder.resultViewThumbnail.setOnClickListener { click(position, list[position][0]) }
    }

    inner class ViewHolder(
        mBinding: ItemVideoStorageListBinding,
        val resultViewThumbnail : ImageView,
        val resultViewName : TextView
    ) : RecyclerView.ViewHolder(mBinding.root)
}
