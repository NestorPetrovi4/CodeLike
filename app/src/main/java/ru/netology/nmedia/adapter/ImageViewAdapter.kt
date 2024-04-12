package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ru.netology.nmedia.R


@SuppressLint("CheckResult")
fun ImageView.loadAvatar(url: String){
    val options = RequestOptions();
    options.transform(RoundedCorners(60))
    getLoadImage(url, options, this)
}

@SuppressLint("CheckResult")
fun ImageView.load(url: String){
    val options = RequestOptions();
    getLoadImage(url, options, this)
}

@SuppressLint("CheckResult")
private fun getLoadImage(url: String, options: RequestOptions, view: ImageView){
    options.placeholder(R.drawable.baseline_refresh_24)
    options.error(R.drawable.baseline_error_outline_24)
    options.timeout(10_000)
    Glide.with(view)
        .load(url)
        .apply(options)
        .into(view)
}