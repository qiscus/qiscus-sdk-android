package com.qiscus.sdk.chat.data.model

/**
 * Created on : August 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class MemberStateEntity(var lastDeliveredCommentId: String = "", var lastReadCommentId: String = "")

data class RoomMemberEntity(val userEntity: UserEntity, val memberStateEntity: MemberStateEntity)