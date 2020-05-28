package kr.co.jsh.feature.storage.photo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.jsh.databinding.ItemImageStorageListBinding
import java.io.File
import java.net.URL

class PhotoStorageAdapter(val click:( Int, String) -> Unit,
                          private var list: ArrayList<List<String>>,
                          private var context: Context)
    :RecyclerView.Adapter<PhotoStorageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, binding.resultImageStorageView, binding.resultImageName)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(list[position][0]).into(holder.resultImageView)
        holder.resultImageName.text = list[position][1]
        holder.resultImageView.setOnClickListener { click(position,  list[position][0]) }
    }

    inner class ViewHolder(
        binding : ItemImageStorageListBinding,
        val resultImageView : ImageView,
        val resultImageName : TextView
    ) : RecyclerView.ViewHolder(binding.root)
}