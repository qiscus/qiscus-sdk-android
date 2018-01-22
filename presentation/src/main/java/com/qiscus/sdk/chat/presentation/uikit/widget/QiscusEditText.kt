package com.qiscus.sdk.chat.presentation.uikit.widget

import android.content.Context
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v13.view.inputmethod.InputContentInfoCompat
import android.support.v4.os.BuildCompat
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.qiscus.manggil.ui.MentionsEditText

/**
 * @author yuana
 * @since 10/27/17
 */
class QiscusEditText : MentionsEditText {

    private var commitListener: CommitListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onCreateInputConnection(info: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(info)
        EditorInfoCompat.setContentMimeTypes(info, arrayOf("image/gif"))
        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
            if (BuildCompat.isAtLeastNMR1() && flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    return@OnCommitContentListener false
                }

            }
            if (commitListener != null) {
                commitListener!!.onCommitContent(inputContentInfo)
            }
            true
        }
        return InputConnectionCompat.createWrapper(ic, info, callback)
    }

    fun setCommitListener(listener: CommitListener) {
        this.commitListener = listener
    }

    interface CommitListener {
        fun onCommitContent(infoCompat: InputContentInfoCompat)
    }
}