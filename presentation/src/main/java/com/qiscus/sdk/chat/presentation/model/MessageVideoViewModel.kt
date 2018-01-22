package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageVideoViewModel(message: FileAttachmentMessage, mimeType: String) : MessageImageViewModel(message, mimeType) {

    private constructor(parcel: Parcel) : this(parcel.readParcelable(Message::class.java.classLoader), parcel.readString()) {
        selected = parcel.readBoolean()
        transfer = parcel.readBoolean()
    }

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83C\uDFA5 " + getString(resId = R.string.qiscus_send_a_video)
        } else {
            "\uD83C\uDFA5 " + message.caption
        }
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

    companion object CREATOR : Parcelable.Creator<MessageVideoViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageVideoViewModel {
            return MessageVideoViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageVideoViewModel?> {
            return arrayOfNulls(size)
        }
    }
}