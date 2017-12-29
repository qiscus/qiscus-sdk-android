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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.presentation.listmessage.ListMessageContract
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.DefaultMessageAdapter
import com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates.*
import com.qiscus.sdk.chat.presentation.model.ButtonViewModel
import com.qiscus.sdk.chat.presentation.model.MessageFileViewModel
import com.qiscus.sdk.chat.presentation.model.MessageImageViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.sendmessage.SendMessageContract
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.uikit.widget.ChatButtonView
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

class ChatRoomActivity : AppCompatActivity(), ListMessageContract.View, SendMessageContract.View,
        ItemClickListener, ItemLongClickListener, ChatButtonView.ChatButtonClickListener {

    private lateinit var listMessagePresenter: ListMessageContract.Presenter
    private lateinit var sendMessagePresenter: SendMessageContract.Presenter

    private val adapter = DefaultMessageAdapter()

    private var roomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        roomId = intent.getStringExtra(INTENT_ROOM_ID)
        if (roomId == null) {
            throw RuntimeException("Please provide room id!")
        }

        adapter.delegatesManager
                .addDelegate(LinkAdapterDelegate(this, this, this))
                .addDelegate(OpponentLinkAdapterDelegate(this, this, this))
                .addDelegate(MultiLineTextAdapterDelegate(this, this, this))
                .addDelegate(OpponentMultiLineTextAdapterDelegate(this, this, this))
                .addDelegate(TextAdapterDelegate(this, this, this))
                .addDelegate(OpponentTextAdapterDelegate(this, this, this))
                .addDelegate(VideoAdapterDelegate(this, this, this))
                .addDelegate(OpponentVideoAdapterDelegate(this, this, this))
                .addDelegate(ImageAdapterDelegate(this, this, this))
                .addDelegate(OpponentImageAdapterDelegate(this, this, this))
                .addDelegate(AudioAdapterDelegate(this, this, this))
                .addDelegate(OpponentAudioAdapterDelegate(this, this, this))
                .addDelegate(FileAdapterDelegate(this, this, this))
                .addDelegate(OpponentFileAdapterDelegate(this, this, this))
                .addDelegate(ContactAdapterDelegate(this, this, this))
                .addDelegate(OpponentContactAdapterDelegate(this, this, this))
                .addDelegate(LocationAdapterDelegate(this, this, this))
                .addDelegate(OpponentLocationAdapterDelegate(this, this, this))
                .addDelegate(CardAdapterDelegate(this, this, this))
                .addDelegate(OpponentCardAdapterDelegate(this, this, this))
                .addDelegate(AccountLinkingAdapterDelegate(this, this, this))
                .addDelegate(OpponentAccountLinkingAdapterDelegate(this, this, this))
                .addDelegate(ButtonsAdapterDelegate(this, this))
                .addDelegate(OpponentButtonsAdapterDelegate(this, this))
                .addDelegate(SystemEventAdapterDelegate(this))

        //Fallback adapter delegate
        adapter.delegatesManager
                .addDelegate(DefaultMultiLineAdapterDelegate(this, this, this))
                .addDelegate(DefaultAdapterDelegate(this, this, this))
                .addDelegate(OpponentDefaultMultiLineAdapterDelegate(this, this, this))
                .addDelegate(OpponentDefaultAdapterDelegate(this, this, this))

        messageRecyclerView.adapter = adapter
        messageRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        messageRecyclerView.layoutManager = layoutManager
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init() {
        val chatRoomActivityComponent = ChatRoomActivityComponent(this)
        listMessagePresenter = chatRoomActivityComponent.listMessagePresenter
        sendMessagePresenter = chatRoomActivityComponent.sendMessagePresenter

        listMessagePresenter.setRoomId(roomId!!)
        listMessagePresenter.start()
    }

    override fun addMessage(messageViewModel: MessageViewModel) {
        adapter.addOrUpdate(messageViewModel)
        messageRecyclerView.smoothScrollToPosition(0)
    }

    override fun updateMessage(messageViewModel: MessageViewModel) {
        adapter.addOrUpdate(messageViewModel)
    }

    override fun removeMessage(messageViewModel: MessageViewModel) {
        adapter.removeMessage(messageViewModel)
    }

    override fun openImageViewer(messageViewModel: MessageImageViewModel) {
        Toast.makeText(this, "open image", Toast.LENGTH_SHORT).show()
    }

    override fun openFileHandler(messageViewModel: MessageFileViewModel) {
        val intent = Intent(Intent.ACTION_VIEW)
        val file = (messageViewModel.message as FileAttachmentMessage).file
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.setDataAndType(Uri.fromFile(file), messageViewModel.mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            intent.setDataAndType(FileProvider.getUriForFile(this,
                    Qiscus.instance.providerAuthorities, file!!), messageViewModel.mimeType)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showError(getString(R.string.qiscus_chat_error_no_handler))
        }

    }

    override fun onItemClick(view: View, position: Int) {
        listMessagePresenter.onMessageClick(adapter.data[position])
    }

    override fun onItemLongClick(view: View, position: Int) {
        listMessagePresenter.onMessageLongClick(adapter.data[position])
    }

    override fun onChatButtonClick(buttonViewModel: ButtonViewModel) {
        listMessagePresenter.onChatButtonClick(buttonViewModel)
    }

    override fun clearTextField() {
        Log.d("ZETRA", "clearTextField")
    }

    override fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        listMessagePresenter.stop()
        sendMessagePresenter.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.onDestroy()
    }
}