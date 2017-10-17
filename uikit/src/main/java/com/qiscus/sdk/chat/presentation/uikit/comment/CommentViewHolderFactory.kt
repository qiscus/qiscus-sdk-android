package com.qiscus.sdk.chat.presentation.uikit.comment

import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.model.CommentViewModel

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentViewHolderFactory {
    fun isUsingThisFactory(commentViewModel: CommentViewModel): Boolean

    fun onCreateViewHolder(parent: ViewGroup?): CommentViewHolder
}