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

package com.qiscus.sdk.chat.sample.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.message.*
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserStatus
import com.qiscus.sdk.chat.domain.interactor.user.ListenUserTyping
import com.qiscus.sdk.chat.domain.interactor.user.PublishTyping
import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.sample.R
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File


/**
 * Created on : September 25, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Context.chatIntent(room: Room): Intent {
    return Intent(this, ChatActivity::class.java).apply {
        putExtra(INTENT_ROOM_ID, room.id)
    }
}

private const val INTENT_ROOM_ID = "room_id"

class ChatActivity : AppCompatActivity() {
    private val useCaseFactory = Qiscus.instance.useCaseFactory

    private val postMessage = useCaseFactory.postMessage()
    private val downloadAttachmentMessage = useCaseFactory.downloadAttachmentMessage()

    private val getMessages = useCaseFactory.getMessages()

    private val listenNewMessage = useCaseFactory.listenNewMessage()

    private val updateMessageState = useCaseFactory.updateMessageState()
    private val listenMessageState = useCaseFactory.listenMessageState()
    private val listenMessageProgress = useCaseFactory.listenFileAttachmentProgress()

    private val publishTyping = useCaseFactory.publishTyping()
    private val listenUserTyping = useCaseFactory.listenUserTyping()

    private val listenUserStatus = useCaseFactory.listenUserStatus()

    private val messageFactory = Qiscus.instance.messageFactory

    private val adapter = ChatAdapter(this)

    private var roomId: String? = null
    private val account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        roomId = intent.getStringExtra(INTENT_ROOM_ID)
        if (roomId == null) {
            throw RuntimeException("Please provide room id!")
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        sendButton.setOnClickListener {
            if (editText.text.isNotBlank()) {
                sendMessage(editText.text.toString())
            }
        }

        sendButton.setOnLongClickListener {
            pickAnImage()
            return@setOnLongClickListener true
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    publishTyping.execute(PublishTyping.Params(roomId!!, false))
                } else {
                    publishTyping.execute(PublishTyping.Params(roomId!!, true))
                }
            }
        })

        loadMessages()
        listenMessage()
        listenTyping()
        listenOnlinePresence()
    }

    private fun listenOnlinePresence() {
        listenUserStatus.execute(ListenUserStatus.Params("rya.meyvriska244@gmail.com"), Action {
            Log.d("ZETRA", "${it.user.name} is ${if (it.online) "Online" else "Offline"} at ${it.lastActive}")
        })
    }

    private fun tryDownloadAnAttachment() {
        val message = adapter.data.first { it is FileAttachmentMessage } as FileAttachmentMessage
        downloadAttachmentMessage.execute(DownloadAttachmentMessage.Params(message), Action {
            Log.d("ZETRA", "Download: $it")
        }, Action {
            it.printStackTrace()
        })
    }

    private fun pickAnImage() {
        val jupukBuilder = JupukBuilder().setMaxCount(1)
        jupukBuilder.enableVideoPicker(true).pickPhoto(this)
    }

    private fun listenTyping() {
        listenUserTyping.execute(ListenUserTyping.Params(roomId!!), Action {
            userTypingText.text = String.format("%s is typing...", it.user.name)
            userTypingText.visibility = if (it.typing) View.VISIBLE else View.GONE
        })
    }

    private fun listenMessage() {
        listenNewMessage.execute(null, Action {
            adapter.addOrUpdate(it)
            if (it.sender.id != account.user.id) {
                updateMessageState.execute(UpdateMessageState.Params(roomId!!, it.messageId, MessageState.READ))
            }
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        })

        listenMessageState.execute(ListenMessageState.Params(roomId!!), Action {
            adapter.addOrUpdate(it)
        })

        listenMessageProgress.execute(null, Action {
            val msg = "${it.state.name} ${it.fileAttachmentMessage.attachmentName} Progress ${it.progress}"
            Log.d("ZETRA", msg)
        })
    }

    private fun loadMessages() {
        getMessages.execute(GetMessages.Params(roomId!!), Action {
            Log.d("ZETRA", "loadMessages: ${it.messages.size}")
            Log.d("ZETRA", "Has more: ${it.hasMoreMessages()}")
            it.messages.reversed().forEach { adapter.addOrUpdate(it) }
            recyclerView.smoothScrollToPosition(adapter.itemCount)
            if (it.hasMoreMessages()) {
                loadMoreMessages(it.messages.last().messageId)
            }
        })
    }

    private fun loadMoreMessages(lastMessageId: MessageId) {
        getMessages.execute(GetMessages.Params(roomId!!, lastMessageId), Action {
            Log.d("ZETRA", "loadMoreMessages: ${it.messages.size}")
            Log.d("ZETRA", "Has more messages: ${it.hasMoreMessages()}")
            it.messages.reversed().forEach { adapter.addOrUpdate(it) }
            recyclerView.smoothScrollToPosition(adapter.itemCount)
            if (it.hasMoreMessages()) {
                loadMoreMessages(it.messages.last().messageId)
            }
        })
    }

    private fun sendMessage(text: String) {
        val message = messageFactory.createTextMessage(roomId!!, text)
        postMessage.execute(PostMessage.Params(message))
        editText.setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == JupukConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return
            }
            val paths = data.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA)
            if (paths.size > 0) {
                val file = File(paths[0])
                val message = messageFactory.createFileAttachmentMessage(roomId!!, file, "caption")
                postMessage.execute(PostMessage.Params(message))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenNewMessage.dispose()
        listenMessageState.dispose()
        listenMessageProgress.dispose()
        listenUserTyping.dispose()
        listenUserStatus.dispose()
    }
}