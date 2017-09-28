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