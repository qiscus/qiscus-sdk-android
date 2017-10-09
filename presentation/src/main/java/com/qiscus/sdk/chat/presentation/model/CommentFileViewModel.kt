package com.qiscus.sdk.chat.presentation.model

import android.support.annotation.ColorInt
import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString
import com.qiscus.sdk.chat.presentation.util.toReadableText
import com.qiscus.sdk.chat.presentation.util.toSpannable

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class CommentFileViewModel
@JvmOverloads constructor(comment: FileAttachmentComment,
                          val mimeType: String,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt mentionAllColor: Int,
                          @ColorInt mentionOtherColor: Int,
                          @ColorInt mentionMeColor: Int,
                          mentionClickListener: MentionClickHandler? = null)

    : CommentViewModel(comment, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    val readableCaption by lazy {
        comment.caption.toReadableText(userRepository)
    }

    val spannableCaption by lazy {
        comment.caption.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }

    override fun determineReadableMessage(): String {
        return if ((comment as FileAttachmentComment).caption.isBlank()) {
            "\uD83D\uDCC4 " + getString(resId = R.string.qiscus_send_attachment)
        } else {
            "\uD83D\uDCC4 " + comment.caption
        }
    }

    override fun determineSpannableMessage(): Spannable {
        return readableMessage.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }
}