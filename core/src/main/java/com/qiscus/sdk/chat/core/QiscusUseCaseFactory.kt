/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.core

import com.qiscus.sdk.chat.domain.interactor.account.*
import com.qiscus.sdk.chat.domain.interactor.comment.*
import com.qiscus.sdk.chat.domain.interactor.file.ListenFileAttachmentProgress
import com.qiscus.sdk.chat.domain.interactor.room.*
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserStatus
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserTyping
import com.qiscus.sdk.chat.domain.interactor.user.PublishTyping

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

    fun listenRoomAdded(): ListenRoomAdded

    fun listenRoomUpdated(): ListenRoomUpdated

    fun listenRoomDeleted(): ListenRoomDeleted


    fun listenNewComment(): ListenNewComment

    fun listenCommentState(): ListenCommentState

    fun listenFileAttachmentProgress(): ListenFileAttachmentProgress

    fun listenCommentDeleted(): ListenCommentDeleted

    fun postComment(): PostComment

    fun updateCommentState(): UpdateCommentState

    fun downloadAttachmentComment(): DownloadAttachmentComment

    fun deleteComment(): DeleteComment

    fun getComments(): GetComments

    fun getMoreComments(): GetMoreComments


    fun listenUserStatus(): ListenUserStatus

    fun listenUserTyping(): ListenUserTyping

    fun publishTyping(): PublishTyping
}