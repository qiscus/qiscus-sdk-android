package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import com.qiscus.sdk.chat.presentation.model.MessageAudioViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.MessageAdapter

/**
 * Created on : December 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class DefaultMessageAdapter : MessageAdapter() {
    open fun onDestroy() {
        val size = data.size()
        (0 until size)
                .filter { data[it] is MessageAudioViewModel }
                .forEach { (data[it] as MessageAudioViewModel).destroy() }
    }
}