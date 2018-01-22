package com.qiscus.sdk.chat.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.util.generateUniqueId

/**
 * Created on : January 22, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class MessageId
@JvmOverloads constructor(val id: String = "", val beforeId: String = "", val uniqueId: String = generateUniqueId()): Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readString())

    override fun equals(other: Any?): Boolean {
        if (other !is MessageId) {
            return false
        }

        return if (id.isBlank()) {
            uniqueId == other.uniqueId
        } else {
            id == other.id || uniqueId == other.uniqueId
        }
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(beforeId)
        parcel.writeString(uniqueId)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageId> {
        override fun createFromParcel(parcel: Parcel): MessageId {
            return MessageId(parcel)
        }

        override fun newArray(size: Int): Array<MessageId?> {
            return arrayOfNulls(size)
        }
    }
}