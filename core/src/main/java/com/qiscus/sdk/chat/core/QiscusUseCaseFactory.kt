package com.qiscus.sdk.chat.core

import com.qiscus.sdk.chat.domain.interactor.account.*
import com.qiscus.sdk.chat.domain.interactor.comment.*
import com.qiscus.sdk.chat.domain.interactor.file.ListenFileAttachmentProgress
import com.qiscus.sdk.chat.domain.interactor.room.*

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface QiscusUseCaseFactory {
    fun requestNonce(): RequestNonce

    fun authenticate(): Authenticate

    fun authenticateWithKey(): AuthenticateWithKey

    fun updateAccount(): UpdateAccount

    fun logout(): Logout

    fun getRoom(): GetRoom

    fun getRoomWithUserId(): GetRoomWithUserId

    fun createGroupRoom(): CreateGroupRoom

    fun getRoomWithChannelId(): GetRoomWithChannelId

    fun getRoomMembers(): GetRoomMembers

    fun getRooms(): GetRooms

    fun getRoomsWithSpecificIds(): GetRoomsWithSpecificIds

    fun listenUserTyping(): ListenUserTyping

    fun publishTyping(): PublishTyping

    fun listenNewComment(): ListenNewComment

    fun listenCommentState(): ListenCommentState

    fun listenFileAttachmentProgress(): ListenFileAttachmentProgress

    fun postComment(): PostComment

    fun updateCommentState(): UpdateCommentState

    fun downloadAttachmentComment(): DownloadAttachmentComment

    fun deleteComment(): DeleteComment

    fun getComments(): GetComments

    fun getMoreComments(): GetMoreComments

    fun listenUserStatus(): ListenUserStatus
}