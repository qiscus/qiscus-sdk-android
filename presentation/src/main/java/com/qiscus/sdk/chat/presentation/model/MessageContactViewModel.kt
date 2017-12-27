package com.qiscus.sdk.chat.presentation.model

import android.text.Spannable
import android.text.SpannableString
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageContactViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val contactName by lazy {
        message.type.payload.optString("name")
    }

    val contactType by lazy {
        message.type.payload.optString("type", "phone")
    }

    val contactValue by lazy {
        message.type.payload.optString("value")
    }

    override fun determineReadableMessage(): String {
        return "\u260E $contactName - $contactValue"
    }

    override fun determineSpannableMessage(): Spannable {
        return SpannableString(readableMessage)
    }
}