package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.util.toReadableText
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageViewModel(val message: Message): Parcelable {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    var mentionClickListener: MentionClickListener? = null
    var selected = false

    val readableMessage by lazy {
        determineReadableMessage()
    }

    val spannableMessage by lazy {
        determineSpannableMessage()
    }

    protected open fun determineReadableMessage(): String {
        return message.text.toReadableText()
    }

    protected open fun determineSpannableMessage(): Spannable {
        return message.text.toSpannable(mentionClickListener = mentionClickListener)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageViewModel {
            return MessageViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageViewModel?> {
            return arrayOfNulls(size)
        }
    }
}