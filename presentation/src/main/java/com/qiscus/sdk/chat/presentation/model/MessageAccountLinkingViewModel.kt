package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageAccountLinkingViewModel(message: Message) : MessageViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    val button by lazy {
        val payload = message.type.payload.optJSONObject("params")
        ButtonAccountLinkingViewModel(payload.optString("button_text"), message.type.rawType, payload)
    }

    override fun determineReadableMessage(): String {
        return message.type.payload.optString("text", message.text)
    }

    override fun determineSpannableMessage(): Spannable {
        return message.type.payload.optString("text", message.text)
                .toSpannable(mentionClickListener = mentionClickListener)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageAccountLinkingViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageAccountLinkingViewModel {
            return MessageAccountLinkingViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageAccountLinkingViewModel?> {
            return arrayOfNulls(size)
        }
    }
}