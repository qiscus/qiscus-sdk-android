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
import com.qiscus.sdk.chat.domain.common.getAttachmentName
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.comment.*
import com.qiscus.sdk.chat.domain.interactor.room.ListenUserTyping
import com.qiscus.sdk.chat.domain.interactor.room.PublishTyping
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
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

    private val postComment = useCaseFactory.postComment()
    private val downloadAttachmentComment = useCaseFactory.downloadAttachmentComment()

    private val getComments = useCaseFactory.getComments()
    private val getMoreComments = useCaseFactory.getMoreComments()

    private val listenNewComment = useCaseFactory.listenNewComment()

    private val updateCommentState = useCaseFactory.updateCommentState()
    private val listenCommentState = useCaseFactory.listenCommentState()
    private val listenCommentProgress = useCaseFactory.listenFileAttachmentProgress()

    private val publishTyping = useCaseFactory.publishTyping()
    private val listenUserTyping = useCaseFactory.listenUserTyping()

    private val commentFactory = Qiscus.instance.commentFactory

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

        loadComments()
        listenComment()
        listenTyping()
    }

    private fun tryDownloadAnAttachment() {
        val comment = adapter.data.first { it is FileAttachmentComment } as FileAttachmentComment
        downloadAttachmentComment.execute(DownloadAttachmentComment.Params(comment), Action {
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
            Log.d("ZETRA", String.format("%s is typing...", it.user.name))
            userTypingText.text = String.format("%s is typing...", it.user.name)
            userTypingText.visibility = if (it.typing) View.VISIBLE else View.GONE
        })
    }

    private fun listenComment() {
        listenNewComment.execute(null, Action {
            adapter.addOrUpdate(it)
            if (it.sender.id != account.user.id) {
                updateCommentState.execute(UpdateCommentState.Params(roomId!!, it.commentId, CommentState.READ))
            }
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        })

        listenCommentState.execute(ListenCommentState.Params(roomId!!), Action {
            if (it is FileAttachmentComment) {
                var msg = if (it.file == null) {
                    it.getAttachmentUrl().getAttachmentName()
                } else {
                    it.file!!.name
                }
                msg += " Changed to ${it.state}"
                Log.d("ZETRA", msg)
            } else {
                Log.d("ZETRA", "${it.message} Changed to ${it.state}")
            }
            adapter.addOrUpdate(it)
        })

        listenCommentProgress.execute(null, Action {
            var msg = it.state.name
            msg += if (it.state == FileAttachmentProgress.State.DOWNLOADING) {
                " " + it.fileAttachmentComment.getAttachmentUrl().getAttachmentName()
            } else {
                " " + it.fileAttachmentComment.file!!.name
            }

            msg += " Progress ${it.progress}"

            Log.d("ZETRA", msg)
        })
    }

    private fun loadComments() {
        getComments.execute(GetComments.Params(roomId!!), Action {
            it.reversed().forEach { adapter.addOrUpdate(it) }
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        })
    }

    private fun sendMessage(message: String) {
        val comment = commentFactory.createTextComment(roomId!!, message)
        postComment.execute(PostComment.Params(comment))
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
                val comment = commentFactory.createFileAttachmentComment(roomId!!, file, "caption")
                postComment.execute(PostComment.Params(comment))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        postComment.dispose()
        getComments.dispose()
        getMoreComments.dispose()
        listenNewComment.dispose()
        listenCommentState.dispose()
        listenCommentProgress.dispose()
        listenUserTyping.dispose()
        publishTyping.dispose()
    }
}