package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.qiscus.sdk.chat.presentation.R
import kotlinx.android.synthetic.main.view_attachment_button.view.*


/**
 * @author yuana
 * @since 10/30/17
 */

class QiscusAttachmentButton : FrameLayout {

    private var mView: View? = null
    private var iconButton: Drawable? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mView = inflateView()

        val a = this.context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.QiscusAttachmentButton,
                0, 0
        )

        try {

//            TODO : styleable
            iconButton = a.getDrawable(R.styleable.QiscusAttachmentButton_btnIcon)

            initDefaultAttrs()

        } finally {
            a.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        displayAttrs()
    }

    private fun displayAttrs() {
        //todo
        attachmentBtnIcon.setImageDrawable(iconButton)
        invalidateAndRequestLayout()
    }

    private fun invalidateAndRequestLayout() {
        invalidate()
        requestLayout()
    }

    private fun initDefaultAttrs() {
//        todo
        if (iconButton == null) iconButton = getDrawable(android.R.drawable.ic_menu_camera)
    }

    private fun getDrawable(@DrawableRes icon: Int) = ContextCompat.getDrawable(context, icon)

    private fun inflateView(): View = getInflater().inflate(R.layout.view_attachment_button, this)

    private fun getInflater(): LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}
