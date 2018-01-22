package com.qiscus.sdk.chat.presentation.listmessage

import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.message.*
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
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
                           private val downloadAttachmentMessage: DownloadAttachmentMessage) : ListMessageContract.Presenter {

    private lateinit var roomId: String
    private var messageSelectedListener: MessageSelectedListener? = null

    override fun start() {
        listenMessageAdded(roomId)
        listenMessageUpdated(roomId)
        listenMessageDeleted(roomId)
        loadMessages(roomId, limit = 20)
    }

    override fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    override fun setMessageSelectedListener(messageSelectedListener: MessageSelectedListener) {
        this.messageSelectedListener = messageSelectedListener
    }

    override fun loadMessages(roomId: String, lastMessageId: MessageId?, limit: Int) {
        getMessages.execute(GetMessages.Params(roomId, lastMessageId, limit), Action {
            it.messages.forEach { view.addMessage(it.toViewModel()) }
        })
    }

    override fun listenMessageAdded(roomId: String) {
        listenNewMessage.execute(null, Action {
            if (it.room.id == roomId) {
                view.addMessage(it.toViewModel())
                updateMessageState(roomId, it.messageId, MessageState.READ)
            }
        })
    }

    override fun listenMessageUpdated(roomId: String) {
        listenMessageState.execute(ListenMessageState.Params(roomId), Action {
            view.updateMessage(it.toViewModel())
        })
    }

    override fun listenMessageDeleted(roomId: String) {
        listenMessageDeleted.execute(ListenMessageDeleted.Params(roomId), Action {
            view.removeMessage(it.toViewModel())
        })
    }

    override fun updateMessageState(roomId: String, lastMessageId: MessageId, messageState: MessageState) {
        updateMessageState.execute(UpdateMessageState.Params(roomId, lastMessageId, messageState))
    }

    override fun onMessageClick(messageViewModel: MessageViewModel) {
        if (view.getSelectedMessages().isNotEmpty() &&
                (messageViewModel is MessageTextViewModel
                        || messageViewModel is MessageFileViewModel
                        || messageViewModel is MessageContactViewModel
                        || messageViewModel is MessageLocationViewModel
                        || messageViewModel is MessageReplyViewModel)) {
            toggleMessageSelection(messageViewModel)
            return
        }

        if (messageViewModel.message.state.intValue > MessageState.SENDING.intValue) {
            when (messageViewModel) {
                is MessageFileViewModel -> handleClickFileMessage(messageViewModel)
                is MessageAccountLinkingViewModel -> handleClickAccountLinking(messageViewModel)
                is MessageContactViewModel -> handleClickContactMessage(messageViewModel)
                is MessageLocationViewModel -> handleClickLocationMessage(messageViewModel)
                is MessageCardViewModel -> handleClickCardMessage(messageViewModel)
            }
        } else if (messageViewModel.message.state == MessageState.FAILED) {
            view.showFailedMessageDialog(messageViewModel)
        } else if (messageViewModel.message.state == MessageState.PENDING
                || messageViewModel.message.state == MessageState.SENDING) {
            view.showPendingMessageDialog(messageViewModel)
        }
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
            val viewModel = it.toViewModel()
            (viewModel as MessageFileViewModel).transfer = false
            view.updateMessage(viewModel)
        }, Action {
            it.printStackTrace()
            messageFileViewModel.transfer = false
        })
    }

    private fun handleClickAccountLinking(messageViewModel: MessageAccountLinkingViewModel) {
        view.openAccountLinkingPage(messageViewModel)
    }

    private fun handleClickContactMessage(messageViewModel: MessageContactViewModel) {
        view.showAddContactDialog(messageViewModel)
    }

    private fun handleClickLocationMessage(messageViewModel: MessageLocationViewModel) {
        view.openMap(messageViewModel)
    }

    private fun handleClickCardMessage(messageViewModel: MessageCardViewModel) {
        view.openUrl(messageViewModel.cardUrl)
    }

    override fun onMessageLongClick(messageViewModel: MessageViewModel) {
        if (view.getSelectedMessages().isEmpty() &&
                (messageViewModel is MessageTextViewModel
                        || messageViewModel is MessageFileViewModel
                        || messageViewModel is MessageContactViewModel
                        || messageViewModel is MessageLocationViewModel
                        || messageViewModel is MessageReplyViewModel)) {
            toggleMessageSelection(messageViewModel)
        }
    }

    private fun toggleMessageSelection(messageViewModel: MessageViewModel) {
        messageViewModel.selected = !messageViewModel.selected
        view.updateMessage(messageViewModel)
        if (messageSelectedListener != null) {
            messageSelectedListener!!.onMessageSelected(view.getSelectedMessages())
        }
    }

    override fun onChatButtonClick(buttonViewModel: ButtonViewModel) {
        when (buttonViewModel) {
            is ButtonPostBackViewModel -> TODO()
            is ButtonLinkViewModel -> view.openUrl(buttonViewModel.url)
        }
    }

    override fun stop() {
        getMessages.dispose()
        listenNewMessage.dispose()
        listenMessageState.dispose()
        listenMessageDeleted.dispose()
        updateMessageState.dispose()
        downloadAttachmentMessage.dispose()
    }
}