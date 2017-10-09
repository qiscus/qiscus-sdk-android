package com.qiscus.sdk.chat.presentation.android.model

import android.support.annotation.ColorInt
import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.android.MentionClickHandler
import com.qiscus.sdk.chat.presentation.android.util.toReadableText
import com.qiscus.sdk.chat.presentation.android.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class CommentViewModel
@JvmOverloads constructor(val comment: Comment,
                          protected val account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          protected val userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt protected val mentionAllColor: Int,
                          @ColorInt protected val mentionOtherColor: Int,
                          @ColorInt protected val mentionMeColor: Int,
                          protected val mentionClickListener: MentionClickHandler? = null) {

    val readableMessage by lazy {
        determineReadableMessage()
    }

    val spannableMessage by lazy {
        determineSpannableMessage()
    }

    open protected fun determineReadableMessage(): String {
        return comment.message.toReadableText(userRepository)
    }

    open protected fun determineSpannableMessage(): Spannable {
        return comment.message.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor,
                mentionMeColor, mentionClickListener)
    }
}