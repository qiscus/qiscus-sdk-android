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
open class ButtonAccountLinkingViewModel(label: String, type: String, payload: JSONObject) : ButtonViewModel(label, type, payload) {

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readJSON()!!)

    val url by lazy {
        payload.optString("url")
    }

    val finishUrl by lazy {
        payload.optString("redirect_url")
    }

    val title by lazy {
        payload.optString("view_title")
    }

    val successMessage by lazy {
        payload.optString("success_message")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(type)
        parcel.writeJSON(payload)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<ButtonAccountLinkingViewModel> {
        override fun createFromParcel(parcel: Parcel): ButtonAccountLinkingViewModel {
            return ButtonAccountLinkingViewModel(parcel)
        }

        override fun newArray(size: Int): Array<ButtonAccountLinkingViewModel?> {
            return arrayOfNulls(size)
        }
    }
}