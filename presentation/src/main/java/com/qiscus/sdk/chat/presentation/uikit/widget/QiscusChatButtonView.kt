package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.model.ButtonViewModel
import com.qiscus.sdk.chat.presentation.R

class ChatButtonView(context: Context, private val buttonViewModel: ButtonViewModel) : FrameLayout(context), View.OnClickListener {
    private lateinit var button: TextView
    private var chatButtonClickListener: ChatButtonClickListener? = null

    init {
        injectViews()
        initLayout()
    }

    private fun injectViews() {
        View.inflate(context, R.layout.view_chat_button, this)
        button = findViewById(R.id.button)
    }

    private fun initLayout() {
        button.text = buttonViewModel.label
        button.setOnClickListener(this)
    }

    override fun setBackground(background: Drawable?) {
        button.background = background
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        button.setBackgroundColor(color)
    }

    fun setTextColor(@ColorInt color: Int) {
        button.setTextColor(color)
    }

    override fun onClick(v: View) {
        if (chatButtonClickListener != null) {
            chatButtonClickListener!!.onChatButtonClick(buttonViewModel)
        }
    }

    fun setChatButtonClickListener(chatButtonClickListener: ChatButtonClickListener) {
        this.chatButtonClickListener = chatButtonClickListener
    }

    interface ChatButtonClickListener {
        fun onChatButtonClick(buttonViewModel: ButtonViewModel)
    }
}