package com.qiscus.sdk.chat.presentation.model

import android.text.Spannable
import android.text.SpannableString
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageSystemEventViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickHandler? = null)
    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

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
}