package com.qiscus.sdk.chat.presentation.model

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.qiscus.sdk.chat.presentation.R
import com.qiscus.sdk.chat.presentation.util.getString

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageImageViewModel
@JvmOverloads constructor(message: FileAttachmentMessage,
                          mimeType: String,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt mentionAllColor: Int,
                          @ColorInt mentionOtherColor: Int,
                          @ColorInt mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)

    : MessageFileViewModel(message, mimeType, account, userRepository, mentionAllColor, mentionOtherColor,
        mentionMeColor, mentionClickListener) {

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83D\uDCF7 " + getString(resId = R.string.qiscus_send_a_photo)
        } else {
            "\uD83D\uDCF7 " + message.caption
        }
    }

    val blurryThumbnail by lazy {
        var i = message.attachmentUrl.indexOf("upload/")
        if (i > 0) {
            i += 7
            var blurryImageUrl = message.attachmentUrl.substring(0, i)
            blurryImageUrl += "w_320,h_320,c_limit,e_blur:300/"
            var file = message.attachmentUrl.substring(i)
            i = file.lastIndexOf('.')
            if (i > 0) {
                file = file.substring(0, i)
            }
            return@lazy blurryImageUrl + file + ".jpg"
        }
        return@lazy message.attachmentUrl
    }
}