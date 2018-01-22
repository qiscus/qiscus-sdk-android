package com.qiscus.sdk.chat.domain.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created on : January 22, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class ParticipantState(var lastDeliveredMessageId: String = "", var lastReadMessageId: String = "") : Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lastDeliveredMessageId)
        parcel.writeString(lastReadMessageId)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<ParticipantState> {
        override fun createFromParcel(parcel: Parcel): ParticipantState {
            return ParticipantState(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantState?> {
            return arrayOfNulls(size)
        }
    }
}