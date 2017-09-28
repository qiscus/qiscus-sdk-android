package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.MemberStateEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.domain.model.MemberState
import com.qiscus.sdk.chat.domain.model.RoomMember

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun RoomMemberEntity.toDomainModel(): RoomMember {
    return RoomMember(
            userEntity.toDomainModel(),
            MemberState(memberStateEntity.lastDeliveredCommentId, memberStateEntity.lastReadCommentId)
    )
}

fun RoomMember.toEntity(): RoomMemberEntity {
    return RoomMemberEntity(
            user.toEntity(),
            MemberStateEntity(memberState.lastDeliveredCommentId, memberState.lastReadCommentId)
    )
}