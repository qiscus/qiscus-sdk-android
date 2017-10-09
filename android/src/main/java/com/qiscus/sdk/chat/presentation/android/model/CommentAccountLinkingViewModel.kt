package com.qiscus.sdk.chat.presentation.android.model

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.android.MentionClickHandler

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class CommentAccountLinkingViewModel
@JvmOverloads constructor(comment: Comment,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickHandler? = null)
    : CommentViewModel(comment, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val button by lazy {
        val payload = comment.type.payload.optJSONObject("params")
        ButtonAccountLinkingViewModel(payload.optString("button_text"), comment.type.rawType, payload)
    }
}