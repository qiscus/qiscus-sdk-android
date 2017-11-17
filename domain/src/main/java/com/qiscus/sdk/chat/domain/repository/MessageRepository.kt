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

package com.qiscus.sdk.chat.domain.repository

import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MessageRepository {
    fun postMessage(message: Message): Completable

    fun downloadAttachmentMessage(message: FileAttachmentMessage): Single<FileAttachmentMessage>

    fun getMessages(roomId: String): Single<List<Message>>

    fun getMessages(roomId: String, lastMessageId: MessageId, limit: Int): Single<List<Message>>

    fun updateMessageState(roomId: String, messageId: MessageId, messageState: MessageState): Completable

    fun deleteMessage(message: Message): Completable

    fun clearData(): Completable
}