package com.qiscus.sdk.chat.presentation.listmessage

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.message.*
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.presentation.mapper.toViewModel
import com.qiscus.sdk.chat.presentation.model.*

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListMessagePresenter(val view: ListMessageContract.View,
                           private val getMessages: GetMessages,
                           private val listenNewMessage: ListenNewMessage,
                           private val listenMessageState: ListenMessageState,
                           private val listenMessageDeleted: ListenMessageDeleted,
                           private val updateMessageState: UpdateMessageState,
                           private val downloadAttachmentMessage: DownloadAttachmentMessage,
                           private @ColorInt val mentionAllColor: Int,
                           private @ColorInt val mentionOtherColor: Int,
                           private @ColorInt val mentionMeColor: Int,
                           private val mentionClickListener: MentionClickListener? = null) : ListMessageContract.Presenter {

    private lateinit var roomId: String

    override fun start() {
        listenMessageAdded(roomId)
        listenMessageUpdated(roomId)
        listenMessageDeleted(roomId)
        loadMessages(roomId, limit = 20)
    }

    override fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    override fun loadMessages(roomId: String, lastMessageId: MessageId?, limit: Int) {
        getMessages.execute(GetMessages.Params(roomId, lastMessageId, limit), Action {
            it.messages.forEach { view.addMessage(toViewModel(it)) }
        })
    }

    override fun listenMessageAdded(roomId: String) {
        listenNewMessage.execute(null, Action {
            if (it.room.id == roomId) {
                view.addMessage(toViewModel(it))
                updateMessageState(roomId, it.messageId, MessageState.READ)
            }
        })
    }

    override fun listenMessageUpdated(roomId: String) {
        listenMessageState.execute(ListenMessageState.Params(roomId), Action {
            view.updateMessage(toViewModel(it))
        })
    }

    override fun listenMessageDeleted(roomId: String) {
        listenMessageDeleted.execute(ListenMessageDeleted.Params(roomId), Action {
            view.removeMessage(toViewModel(it))
        })
    }

    override fun updateMessageState(roomId: String, lastMessageId: MessageId, messageState: MessageState) {
        updateMessageState.execute(UpdateMessageState.Params(roomId, lastMessageId, messageState))
    }

    override fun onMessageClick(messageViewModel: MessageViewModel) {
        when (messageViewModel) {
            is MessageFileViewModel -> handleClickFileMessage(messageViewModel)
            is MessageTextViewModel -> TODO()
            else -> TODO()
        }
    }

    override fun onMessageLongClick(messageViewModel: MessageViewModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChatButtonClick(buttonViewModel: ButtonViewModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleClickFileMessage(messageViewModel: MessageFileViewModel) {
        if ((messageViewModel.message as FileAttachmentMessage).file == null) {
            downloadFileAttachment(messageViewModel)
        } else {
            when (messageViewModel) {
                is MessageVideoViewModel -> view.openFileHandler(messageViewModel)
                is MessageImageViewModel -> view.openImageViewer(messageViewModel)
                is MessageAudioViewModel -> messageViewModel.playAudio()
                else -> view.openFileHandler(messageViewModel)
            }
        }
    }

    private fun downloadFileAttachment(messageFileViewModel: MessageFileViewModel) {
        if (messageFileViewModel.transfer) {
            return
        }

        messageFileViewModel.transfer = true
        downloadAttachmentMessage.execute(DownloadAttachmentMessage.Params(messageFileViewModel.message as FileAttachmentMessage), Action {
            val viewModel = toViewModel(it)
            (viewModel as MessageFileViewModel).transfer = false
            view.updateMessage(viewModel)
        }, Action {
            it.printStackTrace()
            messageFileViewModel.transfer = false
        })
    }

    override fun stop() {
        getMessages.dispose()
        listenNewMessage.dispose()
        listenMessageState.dispose()
        listenMessageDeleted.dispose()
        updateMessageState.dispose()
        downloadAttachmentMessage.dispose()
    }


    private fun toViewModel(message: Message): MessageViewModel {
        return message.toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }
}