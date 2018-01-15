package com.qiscus.sdk.chat.presentation.listmessage

import com.qiscus.sdk.chat.presentation.model.MessageViewModel

/**
 * Created on : January 15, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MessageSelectedListener {
    fun onMessageSelected(selectedMessages: List<MessageViewModel>)
}