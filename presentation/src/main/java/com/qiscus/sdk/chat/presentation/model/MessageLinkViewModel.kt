package com.qiscus.sdk.chat.presentation.model

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.util.extractUrls
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.schinizer.rxunfurl.model.PreviewData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created on : January 02, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageLinkViewModel
@JvmOverloads constructor(message: Message,
                          account: Account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet(),
                          userRepository: UserRepository = Qiscus.instance.component.dataComponent.userRepository,
                          mentionAllColor: Int,
                          mentionOtherColor: Int,
                          mentionMeColor: Int,
                          mentionClickListener: MentionClickListener? = null)
    : MessageTextViewModel(message, account, userRepository, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener) {

    var linkPreviewListener: LinkPreviewListener? = null
    private val webScrapper = Qiscus.instance.component.dataComponent.webScrapper

    var previewData: PreviewData? = null
        private set

    val urls by lazy {
        message.text.extractUrls()
    }

    fun loadLinkPreview() {
        if (previewData != null) {
            if (linkPreviewListener != null) {
                linkPreviewListener!!.onLinkPreviewReady(this)
            }
        } else if (urls.isNotEmpty()) {
            webScrapper.generatePreviewData(urls[0])
                    .doAfterSuccess { previewData -> previewData.url = urls[0] }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        previewData = it
                        if (linkPreviewListener != null) {
                            linkPreviewListener!!.onLinkPreviewReady(this)
                        }
                    }, {})
        }
    }
}