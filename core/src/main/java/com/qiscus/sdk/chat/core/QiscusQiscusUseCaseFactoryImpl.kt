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

import com.qiscus.sdk.chat.core.component.QiscusComponent
import com.qiscus.sdk.chat.domain.interactor.account.*
import com.qiscus.sdk.chat.domain.interactor.message.*
import com.qiscus.sdk.chat.domain.interactor.file.ListenFileAttachmentProgress
import com.qiscus.sdk.chat.domain.interactor.room.*
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserStatus
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserTyping
import com.qiscus.sdk.chat.domain.interactor.user.PublishTyping

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class QiscusQiscusUseCaseFactoryImpl(private val component: QiscusComponent) : QiscusUseCaseFactory {

    override fun requestNonce(): RequestNonce {
        return RequestNonce(
                component.dataComponent.accountRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun authenticate(): Authenticate {
        return Authenticate(
                component.dataComponent.accountRepository,
                component.dataComponent.pubSubClient,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun authenticateWithKey(): AuthenticateWithKey {
        return AuthenticateWithKey(
                component.dataComponent.accountRepository,
                component.dataComponent.pubSubClient,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun updateAccount(): UpdateAccount {
        return UpdateAccount(
                component.dataComponent.accountRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun logout(): Logout {
        return Logout(
                component.dataComponent.accountRepository,
                component.dataComponent.userRepository,
                component.dataComponent.roomRepository,
                component.dataComponent.messageRepository,
                component.dataComponent.pubSubClient,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRoom(): GetRoom {
        return GetRoom(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRoomWithUserId(): GetRoomWithUserId {
        return GetRoomWithUserId(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun createGroupRoom(): CreateGroupRoom {
        return CreateGroupRoom(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRoomWithChannelId(): GetRoomWithChannelId {
        return GetRoomWithChannelId(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRoomParticipants(): GetRoomParticipants {
        return GetRoomParticipants(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRooms(): GetRooms {
        return GetRooms(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getRoomsWithSpecificIds(): GetRoomsWithSpecificIds {
        return GetRoomsWithSpecificIds(
                component.dataComponent.roomRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenRoomAdded(): ListenRoomAdded {
        return ListenRoomAdded(
                component.dataComponent.roomObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenRoomUpdated(): ListenRoomUpdated {
        return ListenRoomUpdated(
                component.dataComponent.roomObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenRoomDeleted(): ListenRoomDeleted {
        return ListenRoomDeleted(
                component.dataComponent.roomObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenNewMessage(): ListenNewMessage {
        return ListenNewMessage(
                component.dataComponent.messageObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenFileAttachmentProgress(): ListenFileAttachmentProgress {
        return ListenFileAttachmentProgress(
                component.dataComponent.fileObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenMessageState(): ListenMessageState {
        return ListenMessageState(
                component.dataComponent.messageObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenMessageDeleted(): ListenMessageDeleted {
        return ListenMessageDeleted(
                component.dataComponent.messageObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun postMessage(): PostMessage {
        return PostMessage(
                component.dataComponent.messageRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun updateMessageState(): UpdateMessageState {
        return UpdateMessageState(
                component.dataComponent.messageRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun downloadAttachmentMessage(): DownloadAttachmentMessage {
        return DownloadAttachmentMessage(
                component.dataComponent.messageRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun deleteMessage(): DeleteMessage {
        return DeleteMessage(
                component.dataComponent.messageRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun getMessages(): GetMessages {
        return GetMessages(
                component.dataComponent.messageRepository,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenUserStatus(): ListenUserStatus {
        return ListenUserStatus(
                component.dataComponent.userObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun listenUserTyping(): ListenUserTyping {
        return ListenUserTyping(
                component.dataComponent.userObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }

    override fun publishTyping(): PublishTyping {
        return PublishTyping(
                component.dataComponent.userObserver,
                component.executorComponent.threadExecutor,
                component.executorComponent.postExecutionThread
        )
    }
}