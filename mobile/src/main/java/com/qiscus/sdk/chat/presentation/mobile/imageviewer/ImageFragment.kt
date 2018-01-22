package com.qiscus.sdk.chat.presentation.mobile.imageviewer

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.presentation.mobile.R
import kotlinx.android.synthetic.main.fragment_qiscus_image.*
import java.io.File

/**
 * Created on : January 18, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

fun Context.imageFragment(imageFile: File): ImageFragment {
    val fragment = ImageFragment()
    val bundle = Bundle()
    bundle.putSerializable(EXTRA_IMAGE_FILE, imageFile)
    fragment.arguments = bundle
    return fragment
}

private const val EXTRA_IMAGE_FILE = "extra_image_file"

class ImageFragment : Fragment() {

    private var imageFile: File? = null
    private var clickListener: ClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        imageView.setOnClickListener {
            if (clickListener != null) {
                clickListener!!.onImageClick()
            }
        }
        return inflater.inflate(R.layout.fragment_qiscus_image, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val activity = activity
        if (activity is ClickListener) {
            clickListener = activity
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imageFile = arguments!!.getSerializable(EXTRA_IMAGE_FILE) as File
        if (imageFile == null && savedInstanceState != null) {
            imageFile = savedInstanceState.getSerializable(EXTRA_IMAGE_FILE) as File
        }

        if (imageFile == null) {
            activity!!.finish()
            return
        }

        Glide.with(this)
                .load(imageFile)
                .into(imageView)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(EXTRA_IMAGE_FILE, imageFile)
    }

    interface ClickListener {
        fun onImageClick()
    }
}