package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageSystemEventViewModel(message: Message) : MessageViewModel(message) {

    private val account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet()

    private constructor(parcel: Parcel) : this(message = parcel.readParcelable(Message::class.java.classLoader)) {
        selected = parcel.readBoolean()
    }

    override fun determineReadableMessage(): String {
        val payload = message.type.payload
        var message = if (payload.optString("subject_email") == account.user.id) {
            getString(resId = R.string.qiscus_you)
        } else {
            payload.optString("subject_username")
        }
        when (payload.optString("type")) {
            "create_room" -> {
                message += " " + getString(resId = R.string.qiscus_created_room)
                message += " '" + payload.optString("room_name") + "'"
            }
            "add_member" -> {
                message += " " + getString(resId = R.string.qiscus_added)
                message += " " + if (payload.optString("object_email") == account.user.id) {
                    getString(resId = R.string.qiscus_you)
                } else {
                    payload.optString("object_username")
                }
            }
            "join_room" -> message += " " + getString(resId = R.string.qiscus_joined_room)
            "remove_member" -> {
                message += " " + getString(resId = R.string.qiscus_removed)
                message += " " + if (payload.optString("object_email") == account.user.id) {
                    getString(resId = R.string.qiscus_you)
                } else {
                    payload.optString("object_username")
                }
            }
            "left_room" -> message += " " + getString(resId = R.string.qiscus_left_room)
            "change_room_name" -> {
                message += " " + getString(resId = R.string.qiscus_changed_room_name)
                message += " '" + payload.optString("room_name") + "'"
            }
            "change_room_avatar" -> message += " " + getString(resId = R.string.qiscus_changed_room_avatar)
            else -> message = this.message.text
        }
        return message
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

    companion object CREATOR : Parcelable.Creator<MessageSystemEventViewModel> {
        override fun createFromParcel(parcel: Parcel): MessageSystemEventViewModel {
            return MessageSystemEventViewModel(parcel)
        }

        override fun newArray(size: Int): Array<MessageSystemEventViewModel?> {
            return arrayOfNulls(size)
        }
    }
}