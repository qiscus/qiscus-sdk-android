package com.qiscus.sdk.chat.presentation.uikit.adapter.message

import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.model.MessageViewModel

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MessageViewHolderFactory {
    fun isUsingThisFactory(messageViewModel: MessageViewModel): Boolean

    fun onCreateViewHolder(parent: ViewGroup?): MessageViewHolder
}