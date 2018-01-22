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
open class MessageCardViewModel(message: Message) : MessageViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    val cardImage by lazy {
        message.type.payload.optString("image")
    }

    val cardTitle by lazy {
        message.type.payload.optString("title")
    }

    val cardDescription by lazy {
        message.type.payload.optString("description")
    }

    val cardUrl by lazy {
        message.type.payload.optString("url")
    }

    val buttons by lazy {
        val buttonsArray = message.type.payload.getJSONArray("buttons")
        val size = buttonsArray.length()
        val buttons = arrayListOf<ButtonViewModel>()
        (0 until size).map { buttonsArray.getJSONObject(it) }
                .mapTo(buttons) {
                    when {
                        it.optString("type") == "link" -> ButtonLinkViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                        it.optString("type") == "postback" -> ButtonPostBackViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                        else -> ButtonViewModel(it.optString("label", "Button"),
                                it.optString("type"), it.optJSONObject("payload"))
                    }
                }
        return@lazy buttons
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

    companion object CREATOR : Parcelable.Creator<MessageCardViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageCardViewModel {
            return MessageCardViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageCardViewModel?> {
            return arrayOfNulls(size)
        }
    }
}