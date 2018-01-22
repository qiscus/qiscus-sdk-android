package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.getExtension
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString
import com.qiscus.sdk.chat.presentation.util.toReadableText
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageFileViewModel(message: FileAttachmentMessage, val mimeType: String) : MessageViewModel(message) {

    protected constructor(parcel: Parcel) : this(parcel.readParcelable(Message::class.java.classLoader), parcel.readString()) {
        selected = parcel.readBoolean()
        transfer = parcel.readBoolean()
    }

    var transferListener: TransferListener? = null
    var transfer: Boolean = false
        set(value) {
            field = value
            if (transferListener != null) {
                transferListener!!.onTransfer(value)
            }
        }

    var progressListener: ProgressListener? = null
    var progress: FileAttachmentProgress? = null
        private set

    private var attachmentProgress = Qiscus.instance.useCaseFactory.listenFileAttachmentProgress()

    val readableCaption by lazy {
        message.caption.toReadableText()
    }

    val spannableCaption by lazy {
        message.caption.toSpannable(mentionClickListener = mentionClickListener)
    }

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83D\uDCC4 " + getString(resId = R.string.qiscus_send_attachment)
        } else {
            "\uD83D\uDCC4 " + message.caption
        }
    }

    override fun determineSpannableMessage(): Spannable {
        return readableMessage.toSpannable(mentionClickListener = mentionClickListener)
    }

    val fileType by lazy {
        val extension = message.attachmentName.getExtension()
        if (extension.isBlank()) {
            return@lazy getString(resId = R.string.qiscus_unknown_file_type)
        }

        return@lazy getString(resId = R.string.qiscus_file_type, formatArgs = *arrayOf(extension.toUpperCase()))
    }

    fun listenAttachmentProgress() {
        if (attachmentProgress.isDisposed()) {
            attachmentProgress = Qiscus.instance.useCaseFactory.listenFileAttachmentProgress()
        }

        attachmentProgress.execute(null, Action {
            if (it.fileAttachmentMessage.messageId == message.messageId) {
                progress = it
                if (progressListener != null) {
                    progressListener!!.onProgress(it)
                }
            }
        })
    }

    fun stopListenAttachmentProgress() {
        attachmentProgress.dispose()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeString(mimeType)
        parcel.writeBoolean(selected)
        parcel.writeBoolean(transfer)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageFileViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageFileViewModel {
            return MessageFileViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageFileViewModel?> {
            return arrayOfNulls(size)
        }
    }
}