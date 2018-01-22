package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageTextViewModel(message: Message) : MessageViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(message, flags)
        parcel.writeBoolean(selected)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageTextViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageTextViewModel {
            return MessageTextViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageTextViewModel?> {
            return arrayOfNulls(size)
        }
    }
}