package com.qiscus.sdk.chat.presentation.model

import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageAccountLinkingViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val button by lazy {
        val payload = message.type.payload.optJSONObject("params")
        ButtonAccountLinkingViewModel(payload.optString("button_text"), message.type.rawType, payload)
    }

    override fun determineReadableMessage(): String {
        return message.type.payload.optString("text", message.text)
    }

    override fun determineSpannableMessage(): Spannable {
        return message.type.payload.optString("text", message.text)
                .toSpannable(account, userRepository, mentionAllColor, mentionOtherColor,
                        mentionMeColor, mentionClickListener)
    }
}