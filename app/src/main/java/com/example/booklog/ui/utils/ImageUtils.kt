package com.example.booklog.ui.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.booklog.R

object ImageUtils {
    fun loadCover(imageView: ImageView, url: String?) {
        val cleanUrl = url?.trim()?.takeIf { it.isNotBlank() }
        if (cleanUrl == null) {
            imageView.setImageResource(R.drawable.bg_cover)
            return
        }

        val model: Any = if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            GlideUrl(
                cleanUrl,
                LazyHeaders.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .build()
            )
        } else {
            cleanUrl
        }

        Glide.with(imageView.context)
            .load(model)
            .placeholder(R.drawable.bg_cover)
            .error(R.drawable.bg_cover)
            .into(imageView)
    }
}
