package com.qiscus.sdk.chat.presentation.model

import com.qiscus.sdk.chat.domain.model.User

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MentionClickListener {
    fun onMentionClick(user: User)
}