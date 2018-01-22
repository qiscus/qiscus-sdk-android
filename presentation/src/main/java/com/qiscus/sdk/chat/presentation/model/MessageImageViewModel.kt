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
open class MessageImageViewModel(message: FileAttachmentMessage, mimeType: String) : MessageFileViewModel(message, mimeType) {

    private constructor(parcel: Parcel) : this(parcel.readParcelable(Message::class.java.classLoader), parcel.readString()) {
        selected = parcel.readBoolean()
        transfer = parcel.readBoolean()
    }

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83D\uDCF7 " + getString(resId = R.string.qiscus_send_a_photo)
        } else {
            "\uD83D\uDCF7 " + message.caption
        }
    }

    val blurryThumbnail by lazy {
        var i = message.attachmentUrl.indexOf("upload/")
        if (i > 0) {
            i += 7
            var blurryImageUrl = message.attachmentUrl.substring(0, i)
            blurryImageUrl += "w_320,h_320,c_limit,e_blur:300/"
            var file = message.attachmentUrl.substring(i)
            i = file.lastIndexOf('.')
            if (i > 0) {
                file = file.substring(0, i)
            }
            return@lazy blurryImageUrl + file + ".jpg"
        }
        return@lazy message.attachmentUrl
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

    companion object CREATOR : Parcelable.Creator<MessageImageViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageImageViewModel {
            return MessageImageViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageImageViewModel?> {
            return arrayOfNulls(size)
        }
    }
}