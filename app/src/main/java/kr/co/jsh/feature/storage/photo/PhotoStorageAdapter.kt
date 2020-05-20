package kr.co.jsh.feature.storage.photo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.jsh.databinding.ItemImageStorageListBinding
import java.net.URL

class PhotoStorageAdapter(val click:( Int) -> Unit, private val imageResult : ArrayList<URL>, private var context: Context)
    :RecyclerView.Adapter<PhotoStorageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageStorageListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, binding.resultStorageView)
    }

    override fun getItemCount(): Int = imageResult.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(imageResult[position]).into(holder.image)
        holder.image.setOnClickListener { click(position) }
    }

    inner class ViewHolder(
        binding : ItemImageStorageListBinding,
        val image : ImageView
    ) : RecyclerView.ViewHolder(binding.root)
}