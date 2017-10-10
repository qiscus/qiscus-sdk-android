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
import com.qiscus.sdk.chat.presentation.listcomment.ListCommentContract
import com.qiscus.sdk.chat.presentation.listcomment.ListCommentPresenter
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.sendcomment.SendCommentContract
import com.qiscus.sdk.chat.presentation.sendcomment.SendCommentPresenter

data class ChatRoomActivityComponent
(
        private val activity: ChatRoomActivity,
        private val sendCommentView: SendCommentContract.View = activity,
        private val listCommentView: ListCommentContract.View = activity,
        private val useCaseFactory: QiscusUseCaseFactory = Qiscus.instance.useCaseFactory,
        private @ColorInt val mentionAllColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_all),
        private @ColorInt val mentionOtherColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_other),
        private @ColorInt val mentionMeColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_me),

        val sendCommentPresenter: SendCommentContract.Presenter =
        SendCommentPresenter(sendCommentView, useCaseFactory.postComment(), Qiscus.instance.commentFactory),

        val listCommentPresenter: ListCommentPresenter =
        ListCommentPresenter(listCommentView, useCaseFactory.getComments(), useCaseFactory.listenNewComment(),
                useCaseFactory.listenCommentState(), useCaseFactory.listenCommentDeleted(), useCaseFactory.updateCommentState(),
                mentionAllColor, mentionOtherColor, mentionMeColor)
)