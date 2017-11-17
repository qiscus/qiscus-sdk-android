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

package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.mapper.toEntity
import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.MessageStateEntity
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.util.FileManager
import com.qiscus.sdk.chat.data.util.PostMessageHandler
import com.qiscus.sdk.chat.domain.model.*
import com.qiscus.sdk.chat.domain.repository.MessageRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageDataRepository(private val messageLocal: MessageLocal,
                            private val messageRemote: MessageRemote,
                            private val fileLocal: FileLocal,
                            private val fileRemote: FileRemote,
                            private val fileManager: FileManager,
                            private val filePublisher: FilePublisher,
                            private val postMessageHandler: PostMessageHandler) : MessageRepository {

    override fun postMessage(message: Message): Completable {
        if (message is FileAttachmentMessage) {
            return postAttachmentMessage(message)
        }

        val messageEntity = message.toEntity()
        return messageRemote.postMessage(messageEntity)
                .doOnSubscribe { messageLocal.saveAndNotify(messageEntity) }
                .doOnSuccess { messageLocal.addOrUpdateMessage(it) }
                .doOnError { handleErrorPostMessage(messageEntity) }
                .toCompletable()
    }

    private fun postAttachmentMessage(message: FileAttachmentMessage): Completable {
        val attachmentMessage = message.toEntity()
        attachmentMessage.file = fileManager.saveFile(message.file!!)

        val progress = FileAttachmentProgress(attachmentMessage.toDomainModel(),
                FileAttachmentProgress.State.UPLOADING, 0)

        return fileRemote.upload(message.file!!, {
            progress.progress = it
            filePublisher.onFileProgressUpdated(progress)
        })
                .doOnSubscribe {
                    fileLocal.saveLocalPath(attachmentMessage.messageId, message.file!!)
                    messageLocal.saveAndNotify(attachmentMessage)
                }
                .doOnSuccess { attachmentMessage.attachmentUrl = it }
                .doOnError { handleErrorPostMessage(attachmentMessage) }
                .flatMap {
                    messageRemote.postMessage(attachmentMessage)
                            .doOnSuccess { messageLocal.addOrUpdateMessage(it) }
                            .doOnError { handleErrorPostMessage(attachmentMessage) }
                }
                .toCompletable()
    }

    private fun handleErrorPostMessage(messageEntity: MessageEntity) {
        messageEntity.state = MessageStateEntity.PENDING
        messageLocal.addOrUpdateMessage(messageEntity)
    }

    override fun downloadAttachmentMessage(message: FileAttachmentMessage): Single<FileAttachmentMessage> {
        val attachmentMessage = message.toEntity()
        val progress = FileAttachmentProgress(attachmentMessage.toDomainModel(),
                FileAttachmentProgress.State.DOWNLOADING, 0)

        return fileRemote.download(attachmentMessage.attachmentUrl, {
            progress.progress = it
            filePublisher.onFileProgressUpdated(progress)
        })
                .doOnSuccess {
                    attachmentMessage.file = it
                    fileLocal.saveLocalPath(attachmentMessage.messageId, attachmentMessage.file!!)
                    messageLocal.addOrUpdateMessage(attachmentMessage)
                }
                .map { attachmentMessage.toDomainModel() }
    }

    override fun getMessages(roomId: String): Single<List<Message>> {
        messageRemote.getMessages(roomId)
                .doOnSuccess { it.forEach { messageLocal.addOrUpdateMessage(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { messageLocal.getMessages(roomId, 20) } }
                .flatMap {
                    if (it.isEmpty() || !isValidChainingMessages(it)) {
                        return@flatMap messageRemote.getMessages(roomId)
                                .doOnSuccess {
                                    it.forEach {
                                        determineMessageState(it)
                                        messageLocal.addOrUpdateMessage(it)
                                    }
                                }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun getMessages(roomId: String, lastMessageId: MessageId, limit: Int): Single<List<Message>> {
        val lastMessage = lastMessageId.toEntity()

        messageRemote.getMessages(roomId, lastMessage, limit)
                .doOnSuccess { it.forEach { messageLocal.addOrUpdateMessage(it) } }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        return Single.defer { Single.fromCallable { messageLocal.getMessages(roomId, lastMessage, limit) } }
                .flatMap {
                    if (it.isEmpty() || !isValidOlderMessages(it, lastMessage)) {
                        return@flatMap messageRemote.getMessages(roomId, lastMessage, limit)
                                .doOnSuccess {
                                    it.forEach {
                                        determineMessageState(it)
                                        messageLocal.addOrUpdateMessage(it)
                                    }
                                }
                    }
                    return@flatMap Single.just(it)
                }.map { it.map { it.toDomainModel() } }
    }

    override fun updateMessageState(roomId: String, messageId: MessageId, messageState: MessageState): Completable {
        val lastMessage = messageId.toEntity()
        if (messageState == MessageState.DELIVERED) {
            return messageRemote.updateLastDeliveredMessage(roomId, lastMessage)
        } else if (messageState == MessageState.READ) {
            return messageRemote.updateLastReadMessage(roomId, lastMessage)
        }
        return Completable.complete()
    }

    override fun deleteMessage(message: Message): Completable {
        val messageEntity = message.toEntity()
        return Completable.defer {
            postMessageHandler.cancelPendingMessage(messageEntity)
            messageLocal.deleteMessage(messageEntity)
            Completable.complete()
        }
    }

    override fun clearData(): Completable {
        return Completable.defer {
            fileLocal.clearData()
            messageLocal.clearData()
            Completable.complete()
        }
    }

    private fun determineMessageState(messageEntity: MessageEntity) {
        if (messageEntity.state.intValue >= MessageStateEntity.ON_SERVER.intValue
                && messageEntity.state.intValue < MessageStateEntity.READ.intValue) {
            val lastRead = messageLocal.getLastReadMessageId(messageEntity.room.id)
            if (lastRead != null && messageEntity.messageId.id <= lastRead.id) {
                messageEntity.state = MessageStateEntity.READ
            } else {
                val lastDelivered = messageLocal.getLastDeliveredMessageId(messageEntity.room.id)
                if (lastDelivered != null && messageEntity.messageId.id <= lastDelivered.id) {
                    messageEntity.state = MessageStateEntity.DELIVERED
                }
            }
        }
    }

    private fun cleanFailedMessages(messages: List<MessageEntity>): List<MessageEntity> {
        return messages.filter { it.messageId.id.isNotBlank() }
    }

    private fun isValidChainingMessages(messages: List<MessageEntity>): Boolean {
        val cleanedMessages = cleanFailedMessages(messages)
        val size = cleanedMessages.size
        return (0 until size - 1)
                .none { cleanedMessages[it].messageId.beforeId != cleanedMessages[it + 1].messageId.id }
    }

    private fun isValidOlderMessages(messages: List<MessageEntity>, lastMessage: MessageIdEntity): Boolean {
        if (messages.isEmpty()) return false

        val cleanedMessages = cleanFailedMessages(messages)
        var containsLastValidMessage = cleanedMessages.isEmpty() || lastMessage.id.isNotBlank()
        val size = cleanedMessages.size

        if (size == 1) {
            return (cleanedMessages[0].messageId.beforeId.isBlank() ||
                    cleanedMessages[0].messageId.beforeId == "0")
                    && lastMessage.beforeId == cleanedMessages[0].messageId.id
        }

        for (i in 0 until size - 1) {
            if (!containsLastValidMessage && cleanedMessages[i].messageId.id == lastMessage.beforeId) {
                containsLastValidMessage = true
            }

            if (cleanedMessages[i].messageId.beforeId != cleanedMessages[i + 1].messageId.id) {
                return false
            }
        }
        return containsLastValidMessage
    }
}