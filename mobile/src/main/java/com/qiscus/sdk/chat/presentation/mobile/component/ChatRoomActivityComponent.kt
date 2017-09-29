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

package com.qiscus.sdk.chat.presentation.mobile.component

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory
import com.qiscus.sdk.chat.presentation.listencomment.ListenCommentContract
import com.qiscus.sdk.chat.presentation.listencomment.ListenCommentPresenter
import com.qiscus.sdk.chat.presentation.mapper.CommentMapper
import com.qiscus.sdk.chat.presentation.mobile.chatroom.ChatRoomActivity
import com.qiscus.sdk.chat.presentation.sendcomment.SendCommentContract
import com.qiscus.sdk.chat.presentation.sendcomment.SendCommentPresenter

data class ChatRoomActivityComponent
(
        private val activity: ChatRoomActivity,
        private val listenCommentView: ListenCommentContract.View = activity,
        private val sendCommentView: SendCommentContract.View = activity,
        private val useCaseFactory: QiscusUseCaseFactory = Qiscus.instance.useCaseFactory,

        val listenCommentPresenter: ListenCommentContract.Presenter =
        ListenCommentPresenter(listenCommentView, useCaseFactory.listenNewComment(), CommentMapper()),

        val sendCommentPresenter: SendCommentContract.Presenter =
        SendCommentPresenter(sendCommentView, useCaseFactory.postComment())
)