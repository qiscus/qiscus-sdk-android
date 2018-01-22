package com.qiscus.sdk.chat.presentation.model

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
open class ButtonLinkViewModel(label: String, type: String, payload: JSONObject) : ButtonViewModel(label, type, payload) {

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readJSON()!!)

    val url by lazy {
        payload.optString("url")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(type)
        parcel.writeJSON(payload)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<ButtonLinkViewModel> {
        override fun createFromParcel(parcel: Parcel): ButtonLinkViewModel {
            return ButtonLinkViewModel(parcel)
        }

        override fun newArray(size: Int): Array<ButtonLinkViewModel?> {
            return arrayOfNulls(size)
        }
    }
}