package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.qiscus.sdk.chat.presentation.R
import kotlinx.android.synthetic.main.view_message_composer.view.*

/**
 * @author yuana
 * @since 10/26/17
 */

class QiscusMessageComposer : FrameLayout {

    private var mView: View? = null
    private var iconEmoticon: Drawable? = null
    private var iconSend: Drawable? = null
    private var iconAttachment: Drawable? = null
    private var animBtnSendSwitch: Animation? = null
    private var isFieldMessageEmpty: Boolean = true

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

        animBtnSendSwitch = AnimationUtils.loadAnimation(context, R.anim.qiscus_simple_grow)

        val a = this.context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.QiscusMessageComposer,
                0, 0
        )

        try {

//            TODO : styleable
            iconEmoticon = a.getDrawable(R.styleable.QiscusMessageComposer_btnEmoticonIcon)
            iconSend = a.getDrawable(R.styleable.QiscusMessageComposer_btnSendIcon)
            iconAttachment = a.getDrawable(R.styleable.QiscusMessageComposer_btnAttachment)

            initDefaultAttrs()

        } finally {
            a.recycle()
        }
    }

    private fun initDefaultAttrs() {
        if (iconEmoticon == null) iconEmoticon = getDrawable(R.drawable.ic_qiscus_emot)
        if (iconSend == null) iconSend = getDrawable(R.drawable.ic_qiscus_send)
        if (iconAttachment == null) iconAttachment = getDrawable(R.drawable.ic_qiscus_attach)
    }

    private fun getDrawable(@DrawableRes icon: Int) = ContextCompat.getDrawable(context, icon)

    override fun onFinishInflate() {
        super.onFinishInflate()
        displayAttrs()
    }

    fun setAction(callback: QiscusCommentComposerListener) {
        commentComposerButtonSend.setOnClickListener {
            callback.onClickSend(it, commentComposerTextField.text.toString())
            commentComposerTextField.setText("")
        }

        commentComposerInsertEmoticon.setOnClickListener {
            callback.onClickInsertEmoticon(it)
        }

        commentComposerTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                callback.onBeforeTextFieldChanged(s, start, count, after)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                onMessageEditTextChanged(s)
                callback.onAfterTextFieldChanged(s)
            }
        })
    }

    private fun onMessageEditTextChanged(message: CharSequence?) {
        if (message == null || message.toString().trim().isEmpty()) {
            if (!isFieldMessageEmpty) {
                isFieldMessageEmpty = true
                commentComposerButtonSend.startAnimation(animBtnSendSwitch)
                commentComposerButtonSend.setImageDrawable(iconAttachment)
            }
        } else {
            if (isFieldMessageEmpty) {
                isFieldMessageEmpty = false
                commentComposerButtonSend.startAnimation(animBtnSendSwitch)
                commentComposerButtonSend.setImageDrawable(iconSend)
            }
        }
    }

    private fun displayAttrs() {
//        todo
        commentComposerInsertEmoticon.setImageDrawable(iconEmoticon)
        commentComposerButtonSend.setImageDrawable(iconAttachment)
        invalidateAndRequestLayout()
    }

    private fun inflateView(): View = getInflater().inflate(R.layout.view_message_composer, this)

    private fun getInflater(): LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private fun invalidateAndRequestLayout() {
        invalidate()
        requestLayout()
    }

    interface QiscusCommentComposerListener {

        fun onClickSend(v: View?, message: String)

        fun onClickInsertEmoticon(v: View?)

        fun onBeforeTextFieldChanged(s: CharSequence?, start: Int, count: Int, after: Int)

        fun onAfterTextFieldChanged(s: Editable?)
    }
}
