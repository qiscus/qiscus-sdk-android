package com.qiscus.sdk.chat.domain.model

/**
 * Created on : August 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class MemberState(var lastDeliveredCommentId: String = "", var lastReadCommentId: String = "")

data class RoomMember(val user: User, val memberState: MemberState)