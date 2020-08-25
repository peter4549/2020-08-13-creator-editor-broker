package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.android.synthetic.main.fragment_single_image_view.view.*

class SingleImageViewFragment(private val uri: String? = null): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_image_view, container, false)
        if (uri != null)
            setImage(view.image_view, uri)
        return view
    }

    private fun setImage(imageView: ImageView, uri: String) {
        Glide.with(imageView.context)
            .load(uri)
            .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transform(CenterCrop(), RoundedCorners(8))
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(null)
            .into(imageView)
    }
}