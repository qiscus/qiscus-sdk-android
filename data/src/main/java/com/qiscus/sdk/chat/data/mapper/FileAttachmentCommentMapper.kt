package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.CommentStateEntity
import com.qiscus.sdk.chat.data.model.CommentTypeEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentCommentEntity
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.domain.model.CommentType
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import java.util.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun FileAttachmentCommentEntity.toDomainModel(): FileAttachmentComment {
    return FileAttachmentComment(
            commentId.toDomainModel(),
            file,
            caption,
            message,
            sender.toDomainModel(),
            Date(nanoTimeStamp / 1000000),
            room.toDomainModel(),
            CommentState.valueOf(state.intValue),
            CommentType(type.rawType, type.payload)
    )
}

fun FileAttachmentComment.toEntity(): FileAttachmentCommentEntity {
    return FileAttachmentCommentEntity(
            commentId.toEntity(),
            file,
            caption,
            message,
            sender.toEntity(),
            date.time * 1000000,
            room.toEntity(),
            CommentStateEntity.valueOf(state.intValue),
            CommentTypeEntity(type.rawType, type.payload)
    )
}