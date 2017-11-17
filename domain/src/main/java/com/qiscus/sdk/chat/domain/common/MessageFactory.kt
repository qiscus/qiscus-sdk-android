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

package com.qiscus.sdk.chat.domain.common

import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import org.json.JSONObject
import java.io.File

/**
 * Created on : September 25, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MessageFactory {
    fun createTextMessage(roomId: String, message: String): Message

    fun createFileAttachmentMessage(roomId: String, file: File, caption: String): FileAttachmentMessage

    fun createCustomMessage(roomId: String, defaultTextMessage: String, type: String, content: JSONObject): Message
}