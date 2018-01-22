package com.qiscus.sdk.chat.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.util.readJSON
import com.qiscus.sdk.chat.domain.util.writeJSON
import org.json.JSONObject

/**
 * Created on : January 22, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class MessageType(val rawType: String, val payload: JSONObject) : Parcelable {
    constructor(rawType: String) : this(rawType, JSONObject())

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readJSON()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(rawType)
        parcel.writeJSON(payload)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<MessageType> {
        override fun createFromParcel(parcel: Parcel): MessageType {
            return MessageType(parcel)
        }

        override fun newArray(size: Int): Array<MessageType?> {
            return arrayOfNulls(size)
        }
    }
}