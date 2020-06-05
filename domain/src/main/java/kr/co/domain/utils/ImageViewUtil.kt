package kr.co.domain.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kr.co.domain.R
import timber.log.Timber

fun ImageView.loadUriCenterCrop(uri : Uri) {
    val options = RequestOptions()
    GlideApp.with(this).load(uri).apply(options.centerCrop()).into(this)
    Timber.d("glide loaded %s", uri.toString())
}

fun ImageView.loadUrlCenterCrop(url : String) {
    val options = RequestOptions()
    Timber.d("loaded %s", url)
    GlideApp.with(this).load(url).apply(options.centerCrop()).into(this)
}

fun ImageView.loadUri(uri : Uri) {
    GlideApp.with(this).load(uri).into(this)
}

fun ImageView.loadUrl(url : String?) {
    url?.let {
        GlideApp.with(this).load(it).into(this)
    }
}

fun ImageView.loadUrl(url : String?, requestOptions: RequestOptions ) {
    url?.let {
        GlideApp.with(this).setDefaultRequestOptions(requestOptions).load(it).into(this)
    }
}

