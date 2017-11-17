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

package com.qiscus.sdk.chat.presentation.sendmessage

import com.qiscus.sdk.chat.domain.common.MessageFactory
import com.qiscus.sdk.chat.domain.interactor.message.PostMessage
import java.io.File

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SendMessagePresenter(val view: SendMessageContract.View,
                           private val useCase: PostMessage,
                           private val messageFactory: MessageFactory) : SendMessageContract.Presenter {

    override fun start() {}

    override fun stop() {
        useCase.dispose()
    }

    override fun sendMessage(roomId: String, message: String) {
        useCase.execute(PostMessage.Params(messageFactory.createTextMessage(roomId, message)))
        view.clearTextField()
    }

    override fun sendFileMessage(roomId: String, file: File, caption: String) {
        useCase.execute(PostMessage.Params(messageFactory.createFileAttachmentMessage(roomId, file, caption)))
    }
}