package com.qiscus.sdk.chat.presentation.model

import android.support.annotation.ColorInt
import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.util.toReadableText
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageViewModel
@JvmOverloads constructor(val message: Message,
                          protected val account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          protected val userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt protected val mentionAllColor: Int,
                          @ColorInt protected val mentionOtherColor: Int,
                          @ColorInt protected val mentionMeColor: Int,
                          protected val mentionClickListener: MentionClickListener? = null) {

    val readableMessage by lazy {
        determineReadableMessage()
    }

    val spannableMessage by lazy {
        determineSpannableMessage()
    }

    protected open fun determineReadableMessage(): String {
        return message.text.toReadableText(userRepository)
    }

    protected open fun determineSpannableMessage(): Spannable {
        return message.text.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor,
                mentionMeColor, mentionClickListener)
    }

    var selected = false
}