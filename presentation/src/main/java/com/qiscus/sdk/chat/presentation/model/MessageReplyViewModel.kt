package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import com.qiscus.sdk.chat.domain.model.*
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.util.toReadableText
import com.qiscus.sdk.chat.presentation.util.toSpannable
import java.util.*

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageReplyViewModel(message: Message) : MessageViewModel(message) {

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    val repliedMessage by lazy {
        val payload = message.type.payload
        MessageViewModel(
                Message(MessageId(payload.optString("replied_comment_id")),
                        payload.optString("replied_comment_message"),
                        User(payload.optString("replied_comment_sender_email"), payload.optString("replied_comment_sender_username")),
                        Date(), message.room, MessageState.ON_SERVER,
                        MessageType(payload.optString("replied_comment_type"),
                                payload.optJSONObject("replied_comment_payload")))
        )
    }

    override fun determineReadableMessage(): String {
        return message.type.payload.optString("text", message.text).toReadableText()
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

    companion object CREATOR : Parcelable.Creator<MessageReplyViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageReplyViewModel {
            return MessageReplyViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageReplyViewModel?> {
            return arrayOfNulls(size)
        }
    }
}