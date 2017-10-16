package com.qiscus.sdk.chat.presentation.uikit

import com.qiscus.sdk.chat.domain.model.User

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MentionClickHandler {
    fun onMentionClick(user: User)
}