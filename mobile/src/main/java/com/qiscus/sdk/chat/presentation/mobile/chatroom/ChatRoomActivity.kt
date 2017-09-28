package com.qiscus.sdk.chat.presentation.mobile.chatroom

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.qiscus.sdk.chat.presentation.listencomment.ListenCommentContract
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.mobile.component.ChatRoomActivityComponent
import com.qiscus.sdk.chat.presentation.model.CommentView
import com.qiscus.sdk.chat.presentation.sendcomment.SendCommentContract
import kotlinx.android.synthetic.main.activity_chat_room.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ChatRoomActivity : AppCompatActivity(), ListenCommentContract.View, SendCommentContract.View {
    private lateinit var listenCommentPresenter: ListenCommentContract.Presenter
    private lateinit var sendCommentPresenter: SendCommentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        init()
        sendMessageButton.setOnClickListener({
            sendCommentPresenter.sendComment("Halo ini pesan ku...")
        })
    }

    private fun init() {
        val chatRoomActivityComponent = ChatRoomActivityComponent(this)
        listenCommentPresenter = chatRoomActivityComponent.listenCommentPresenter
        sendCommentPresenter = chatRoomActivityComponent.sendCommentPresenter
    }

    override fun onStart() {
        super.onStart()
        listenCommentPresenter.start()
    }

    override fun onNewComment(commentView: CommentView) {
        Log.d("ZETRA", "new comment: $commentView")
    }

    override fun clearTextField() {
        Log.d("ZETRA", "clearTextField")
    }

    override fun onStop() {
        super.onStop()
        listenCommentPresenter.stop()
    }
}