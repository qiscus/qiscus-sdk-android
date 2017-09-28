package com.qiscus.sdk.chat.presentation.sendcomment

import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface SendCommentContract {
    interface View : BaseView {
        fun clearTextField()
    }

    interface Presenter : BasePresenter {
        fun sendComment(message: String)
    }
}
