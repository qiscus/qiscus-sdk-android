package com.qiscus.sdk.chat.presentation.model

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
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
open class CommentVideoViewModel
@JvmOverloads constructor(comment: FileAttachmentComment,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt mentionAllColor: Int,
                          @ColorInt mentionOtherColor: Int,
                          @ColorInt mentionMeColor: Int,
                          mentionClickListener: MentionClickHandler? = null)

    : CommentFileViewModel(comment, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    override fun determineReadableMessage(): String {
        return if ((comment as FileAttachmentComment).caption.isBlank()) {
            "\uD83C\uDFA5 " + getString(resId = R.string.qiscus_send_a_video)
        } else {
            "\uD83C\uDFA5 " + comment.caption
        }
    }
}