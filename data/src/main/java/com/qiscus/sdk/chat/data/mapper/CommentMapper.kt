package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.*
import com.qiscus.sdk.chat.domain.model.*
import java.util.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun CommentIdEntity.toDomainModel(): CommentId {
    return CommentId(id, commentBeforeId, uniqueId)
}

fun CommentId.toEntity(): CommentIdEntity {
    return CommentIdEntity(id, commentBeforeId, uniqueId)
}

fun CommentEntity.toDomainModel(): Comment {
    if (this is FileAttachmentCommentEntity) {
        return toDomainModel()
    }
    return Comment(
            commentId.toDomainModel(),
            message,
            sender.toDomainModel(),
            Date(nanoTimeStamp/1000000),
            room.toDomainModel(),
            CommentState.valueOf(state.intValue),
            CommentType(type.rawType, type.payload)
    )
}

fun Comment.toEntity(): CommentEntity {
    if (this is FileAttachmentComment) {
        return toEntity()
    }

    return CommentEntity(
            commentId.toEntity(),
            message,
            sender.toEntity(),
            date.time * 1000000,
            room.toEntity(),
            CommentStateEntity.valueOf(state.intValue),
            CommentTypeEntity(type.rawType, type.payload)
    )
}

fun CommentEntity.transformToTypedCommentEntity(): CommentEntity {
    if (type.rawType == "file_attachment" || (message.startsWith("[file]") && message.endsWith("[/file]"))) {
        return FileAttachmentCommentEntity(
                commentId,
                null,
                type.payload.optString("caption", ""),
                message,
                sender,
                nanoTimeStamp,
                room,
                CommentStateEntity.valueOf(state.intValue),
                CommentTypeEntity(type.rawType, type.payload)
        )
    }
    return this
}