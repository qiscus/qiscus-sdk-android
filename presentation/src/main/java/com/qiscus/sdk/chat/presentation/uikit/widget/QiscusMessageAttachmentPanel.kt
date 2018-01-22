package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.qiscus.sdk.chat.presentation.R
import kotlinx.android.synthetic.main.view_message_attachment_panel.view.*

/**
 * @author yuana
 * @since 10/27/17
 */
class QiscusMessageAttachmentPanel : LinearLayout {

    private var mView: View? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mView = inflateView()

        val a = this.context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.QiscusMessageAttachmentPanel,
                0, 0
        )

        try {

//            TODO : styleable

            initDefaultAttrs()

        } finally {
            a.recycle()
        }

        btnAttachmentClose.setOnClickListener { Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show() }
    }

    private fun initDefaultAttrs() {
        //todo
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        displayAttrs()
    }

    private fun displayAttrs() {
        //todo
        invalidateAndRequestLayout()
    }

    private fun invalidateAndRequestLayout() {
        invalidate()
        requestLayout()
    }

    private fun inflateView(): View = getInflater().inflate(R.layout.view_message_attachment_panel, this)

    private fun getInflater(): LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}
