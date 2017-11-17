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

package com.qiscus.sdk.chat.presentation.mobile.chatroom

import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory
import com.qiscus.sdk.chat.presentation.listmessage.ListMessageContract
import com.qiscus.sdk.chat.presentation.listmessage.ListMessagePresenter
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.sendmessage.SendMessageContract
import com.qiscus.sdk.chat.presentation.sendmessage.SendMessagePresenter

data class ChatRoomActivityComponent
(
        private val activity: ChatRoomActivity,
        private val sendMessageView: SendMessageContract.View = activity,
        private val listMessageView: ListMessageContract.View = activity,
        private val useCaseFactory: QiscusUseCaseFactory = Qiscus.instance.useCaseFactory,
        private @ColorInt val mentionAllColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_all),
        private @ColorInt val mentionOtherColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_other),
        private @ColorInt val mentionMeColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_me),

        val sendMessagePresenter: SendMessageContract.Presenter =
        SendMessagePresenter(sendMessageView, useCaseFactory.postMessage(), Qiscus.instance.messageFactory),

        val listMessagePresenter: ListMessagePresenter =
        ListMessagePresenter(listMessageView, useCaseFactory.getMessages(), useCaseFactory.listenNewMessage(),
                useCaseFactory.listenMessageState(), useCaseFactory.listenMessageDeleted(), useCaseFactory.updateMessageState(),
                mentionAllColor, mentionOtherColor, mentionMeColor)
)