package kr.co.jsh.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.byox.drawview.views.DrawView
import kr.co.domain.utils.loadResource

@BindingAdapter("scrollAdapter")
fun setScrollAdapter(recyclerView: RecyclerView, listener: RecyclerView.OnScrollListener){
    recyclerView.addOnScrollListener(listener)
}

@BindingAdapter("drawListener")
fun setDrawListener(view: DrawView, listener: DrawView.OnDrawViewListener){
    view.setOnDrawViewListener(listener)
}

@BindingAdapter("imgLoad")
fun setImgLoad(imageView: ImageView, resid: Int){
    imageView.loadResource(resid)
}