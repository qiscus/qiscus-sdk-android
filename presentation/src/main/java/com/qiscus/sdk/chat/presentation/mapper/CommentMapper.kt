package com.qiscus.sdk.chat.presentation.mapper

import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.presentation.model.CommentView

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentMapper : Mapper<CommentView, Comment> {
    override fun mapToView(type: Comment): CommentView {
        return CommentView(type.sender.name)
    }
}