package kr.co.jsh.feature.photoStorage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.domain.utils.loadUrl
import kr.co.jsh.databinding.ItemPhotoStorageListBinding

class PhotoStorageAdapter(val click:( Int, String) -> Unit,
                          private var list: ArrayList<List<String>>,
                          private var context: Context)
    :RecyclerView.Adapter<PhotoStorageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, binding.resultPhotoStorageView, binding.resultPhotoName)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.resultPhotoView.loadUrl(list[position][0])
        holder.resultPhotoName.text = list[position][1]
        holder.resultPhotoView.setOnClickListener { click(position,  list[position][0]) }
    }

    inner class ViewHolder(
        binding : ItemPhotoStorageListBinding,
        val resultPhotoView : ImageView,
        val resultPhotoName : TextView
    ) : RecyclerView.ViewHolder(binding.root)
}