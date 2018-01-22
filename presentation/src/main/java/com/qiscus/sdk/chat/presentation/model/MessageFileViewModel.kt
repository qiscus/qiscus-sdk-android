package com.qiscus.sdk.chat.presentation.model

import android.support.annotation.ColorInt
import android.text.Spannable
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.util.getExtension
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.domain.repository.UserRepository
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
open class MessageFileViewModel
@JvmOverloads constructor(message: FileAttachmentMessage,
                          val mimeType: String,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          @ColorInt mentionAllColor: Int,
                          @ColorInt mentionOtherColor: Int,
                          @ColorInt mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)

    : MessageViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    var transferListener: TransferListener? = null
    var transfer: Boolean = false
        set(value) {
            field = value
            if (transferListener != null) {
                transferListener!!.onTransfer(value)
            }
        }

    var progressListener: ProgressListener? = null
    var progress: FileAttachmentProgress? = null
        private set

    private var attachmentProgress = Qiscus.instance.useCaseFactory.listenFileAttachmentProgress()

    val readableCaption by lazy {
        message.caption.toReadableText(userRepository)
    }

    val spannableCaption by lazy {
        message.caption.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }

    override fun determineReadableMessage(): String {
        return if ((message as FileAttachmentMessage).caption.isBlank()) {
            "\uD83D\uDCC4 " + getString(resId = R.string.qiscus_send_attachment)
        } else {
            "\uD83D\uDCC4 " + message.caption
        }
    }

    override fun determineSpannableMessage(): Spannable {
        return readableMessage.toSpannable(account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }

    val fileType by lazy {
        val extension = message.attachmentName.getExtension()
        if (extension.isBlank()) {
            return@lazy getString(resId = R.string.qiscus_unknown_file_type)
        }

        return@lazy getString(resId = R.string.qiscus_file_type, formatArgs = *arrayOf(extension.toUpperCase()))
    }

    fun listenAttachmentProgress() {
        if (attachmentProgress.isDisposed()) {
            attachmentProgress = Qiscus.instance.useCaseFactory.listenFileAttachmentProgress()
        }

        attachmentProgress.execute(null, Action {
            if (it.fileAttachmentMessage.messageId == message.messageId) {
                progress = it
                if (progressListener != null) {
                    progressListener!!.onProgress(it)
                }
            }
        })
    }

    fun stopListenAttachmentProgress() {
        attachmentProgress.dispose()
    }
}