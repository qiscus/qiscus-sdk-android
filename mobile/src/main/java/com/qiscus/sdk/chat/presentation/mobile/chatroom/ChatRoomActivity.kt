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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.presentation.listmessage.ListMessageContract
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder.DefaultViewHolderFactory
import com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder.ImageViewHolderFactory
import com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder.TextViewHolderFactory
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.sendmessage.SendMessageContract
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageAdapter
import kotlinx.android.synthetic.main.activity_chat_room.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Context.chatRoomIntent(room: Room): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_ROOM_ID, room.id)
    }
}

fun Context.chatRoomIntent(roomId: String): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_ROOM_ID, roomId)
    }
}

private const val INTENT_ROOM_ID = "room_id"

class ChatRoomActivity : AppCompatActivity(), ListMessageContract.View, SendMessageContract.View {

    private lateinit var listMessagePresenter: ListMessageContract.Presenter
    private lateinit var sendMessagePresenter: SendMessageContract.Presenter

    private val adapter = MessageAdapter()

    private var roomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        roomId = intent.getStringExtra(INTENT_ROOM_ID)
        if (roomId == null) {
            throw RuntimeException("Please provide room id!")
        }


        adapter.registerViewHolderFactory(TextViewHolderFactory(this))
        adapter.registerViewHolderFactory(ImageViewHolderFactory(this))
        adapter.registerViewHolderFactory(DefaultViewHolderFactory(this))

        messageRecyclerView.adapter = adapter
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.setHasFixedSize(true)

        init()
    }

    private fun init() {
        val chatRoomActivityComponent = ChatRoomActivityComponent(this)
        listMessagePresenter = chatRoomActivityComponent.listMessagePresenter
        sendMessagePresenter = chatRoomActivityComponent.sendMessagePresenter

        listMessagePresenter.setRoomId(roomId!!)
        listMessagePresenter.start()
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    override fun addMessage(messageViewModel: MessageViewModel) {
        adapter.addOrUpdate(messageViewModel)
        messageRecyclerView.smoothScrollToPosition(adapter.itemCount)
    }

    override fun updateMessage(messageViewModel: MessageViewModel) {
        adapter.addOrUpdate(messageViewModel)
    }

    override fun removeMessage(messageViewModel: MessageViewModel) {
        adapter.removeMessage(messageViewModel)
    }

    override fun clearTextField() {
        Log.d("ZETRA", "clearTextField")
    }

    override fun onStop() {
        super.onStop()
        listMessagePresenter.stop()
        sendMessagePresenter.stop()
    }
}