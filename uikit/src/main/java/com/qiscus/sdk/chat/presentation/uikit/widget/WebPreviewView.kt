package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.support.annotation.ColorInt
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.presentation.uikit.R
import com.qiscus.sdk.chat.presentation.uikit.util.randomColor
import com.schinizer.rxunfurl.model.PreviewData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WebPreviewView : LinearLayout {

    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var description: TextView
    private var previewData: PreviewData? = null

    private val webScrapper = Qiscus.instance.component.dataComponent.webScrapper

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        injectViews()
        applyAttrs(context, attrs)
    }

    private fun injectViews() {
        View.inflate(context, R.layout.view_web_preview, this)
        image = findViewById(R.id.image)
        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
    }

    private fun applyAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WebPreviewView, 0, 0)
        val titleColor: Int
        val descColor: Int
        try {
            titleColor = a.getColor(R.styleable.WebPreviewView_titleColor, Color.WHITE)
            descColor = a.getColor(R.styleable.WebPreviewView_descColor, Color.WHITE)
        } finally {
            a.recycle()
        }

        title.setTextColor(titleColor)
        description.setTextColor(descColor)

        initLayout()
    }

    private fun initLayout() {
        bind(previewData)
        setOnClickListener { openUrl() }
    }

    fun bind(previewData: PreviewData?) {
        this.previewData = previewData
        if (previewData == null) {
            visibility = View.GONE
        } else {
            image.setBackgroundColor(randomColor())
            if (previewData.images.size > 0) {
                Glide.with(this)
                        .load(previewData.images[0].source)
                        .into(image)
            } else {
                Glide.with(this).load("clear it").into(image)
            }
            title.text = when {
                previewData.title.isEmpty() -> context.getString(R.string.qiscus_web_preview_default_title)
                else -> previewData.title
            }
            description.text = when {
                previewData.description.isEmpty() -> context.getString(R.string.qiscus_web_preview_default_description)
                else -> previewData.description
            }
            visibility = View.VISIBLE
        }
    }

    private fun openUrl() {
        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.qiscus_primary))
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .build()
                .launchUrl(context, Uri.parse(previewData!!.url))
    }

    fun setTitleColor(@ColorInt color: Int) {
        title.setTextColor(color)
    }

    fun setDescriptionColor(@ColorInt color: Int) {
        description.setTextColor(color)
    }

    fun load(url: String) {
        visibility = View.GONE
        if (previewData == null || previewData!!.url != url) {
            webScrapper.generatePreviewData(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        this@WebPreviewView.previewData = previewData
                        bind(this@WebPreviewView.previewData)
                    }, {})
        } else {
            bind(previewData)
        }
    }

    fun clearView() {
        previewData = null
        bind(null)
    }
}
