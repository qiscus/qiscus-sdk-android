package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageContactViewModel(message: Message) : MessageViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    val contactName by lazy {
        message.type.payload.optString("name")
    }

    val contactType by lazy {
        message.type.payload.optString("type", "phone")
    }

    val contactValue by lazy {
        message.type.payload.optString("value")
    }

    override fun determineReadableMessage(): String {
        return "\u260E $contactName - $contactValue"
    }

    override fun determineSpannableMessage(): Spannable {
        return SpannableString(readableMessage)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageContactViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageContactViewModel {
            return MessageContactViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageContactViewModel?> {
            return arrayOfNulls(size)
        }
    }
}