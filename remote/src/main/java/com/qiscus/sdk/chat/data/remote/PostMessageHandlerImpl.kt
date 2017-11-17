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

package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.MessageStateEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentMessageEntity
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.util.PostMessageHandler
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class PostMessageHandlerImpl(private val messageLocal: MessageLocal,
                             private val messageRemote: MessageRemote,
                             private val fileRemote: FileRemote,
                             private val filePublisher: FilePublisher) : PostMessageHandler {
    private val pendingTask = HashMap<MessageIdEntity, Disposable>()

    override fun tryResendPendingMessage() {
        Observable.defer { Observable.fromCallable { messageLocal.getPendingMessages() } }
                .flatMap { Observable.fromIterable(it) }
                .filter { !pendingTask.containsKey(it.messageId) }
                .doOnNext { resendMessage(it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun cancelPendingMessage(message: MessageEntity) {
        if (pendingTask.containsKey(message.messageId)) {
            val disposable = pendingTask[message.messageId]
            if (disposable!!.isDisposed) {
                disposable.dispose()
            }
            pendingTask.remove(message.messageId)
        }
    }

    private fun resendMessage(message: MessageEntity) {
        message.state = MessageStateEntity.SENDING
        messageLocal.addOrUpdateMessage(message)

        if (message is FileAttachmentMessageEntity) {
            resendFileMessage(message)
            return
        }

        val disposable = messageRemote.postMessage(message)
                .doOnSuccess { onSuccessSendingMessage(it) }
                .doOnError { onErrorSendingMessage(message, it) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        pendingTask.put(message.messageId, disposable)
    }

    private fun resendFileMessage(message: FileAttachmentMessageEntity) {
        if (!message.attachmentUrl.isBlank()) {//We have upload it, just need to send text
            val disposable = messageRemote.postMessage(message)
                    .doOnSuccess { onSuccessSendingMessage(it) }
                    .doOnError { onErrorSendingMessage(message, it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({}, {})

            pendingTask.put(message.messageId, disposable)
            return
        }

        if (message.file == null || !message.file!!.exists()) {//File have been deleted, so can not upload it anymore
            message.state = MessageStateEntity.FAILED
            messageLocal.addOrUpdateMessage(message)
            pendingTask.remove(message.messageId)
            return
        }

        //Reuploading the file
        val progress = FileAttachmentProgress(message.toDomainModel(), FileAttachmentProgress.State.UPLOADING, 0)
        val disposable = fileRemote.upload(message.file!!, {
            progress.progress = it
            filePublisher.onFileProgressUpdated(progress)
        })
                .doOnSuccess { message.attachmentUrl = it }
                .doOnError { onErrorSendingMessage(message, it) }
                .flatMap {
                    messageRemote.postMessage(message)
                            .doOnSuccess { onSuccessSendingMessage(it) }
                            .doOnError { onErrorSendingMessage(message, it) }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})

        pendingTask.put(message.messageId, disposable)
    }

    private fun onSuccessSendingMessage(message: MessageEntity) {
        pendingTask.remove(message.messageId)
        messageLocal.addOrUpdateMessage(message)
    }

    private fun onErrorSendingMessage(message: MessageEntity, throwable: Throwable) {
        pendingTask.remove(message.messageId)

        var state = MessageStateEntity.PENDING
        if (throwable is HttpException) { //Error response from server
            //Means something wrong with server, e.g user is not member of these room anymore
            if (throwable.code() >= 400) {
                state = MessageStateEntity.FAILED
            }
        }

        message.state = state
        messageLocal.addOrUpdateMessage(message)
    }
}