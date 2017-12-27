package com.qiscus.sdk.chat.presentation.listroom

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.message.GetMessages
import com.qiscus.sdk.chat.domain.interactor.message.ListenNewMessage
import com.qiscus.sdk.chat.domain.interactor.room.*
import com.qiscus.sdk.chat.presentation.model.MentionClickListener
import com.qiscus.sdk.chat.presentation.mapper.toViewModel
import com.qiscus.sdk.chat.presentation.model.RoomViewModel

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListRoomPresenter(val view: ListRoomContract.View,
                        private val getRoom: GetRoom,
                        private val getRooms: GetRooms,
                        private val getMessages: GetMessages,
                        private val listenNewMessage: ListenNewMessage,
                        private val listenRoomAdded: ListenRoomAdded,
                        private val listenRoomUpdated: ListenRoomUpdated,
                        private val listenRoomDeleted: ListenRoomDeleted,
                        private @ColorInt val mentionAllColor: Int,
                        private @ColorInt val mentionOtherColor: Int,
                        private @ColorInt val mentionMeColor: Int,
                        private val mentionClickListener: MentionClickListener? = null) : ListRoomContract.Presenter {

    override fun start() {
        listenNewMessage()
        listenRoomAdded()
        listenRoomUpdated()
        listenRoomDeleted()
        loadRooms()
    }

    private fun loadLastMessage(roomViewModel: RoomViewModel, onSuccess: Action<RoomViewModel>) {
        getMessages.execute(GetMessages.Params(roomViewModel.room.id, limit = 1), Action {
            if (it.messages.isNotEmpty()) {
                roomViewModel.lastMessage = it.messages.first()
                        .toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
            }
            onSuccess.call(roomViewModel)
        })
    }

    private fun listenNewMessage() {
        listenNewMessage.execute(null, Action {
            view.addOrUpdateRoom(RoomViewModel(it.room,
                    it.toViewModel(mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)))
        })
    }

    override fun loadRooms(page: Int, limit: Int) {
        getRooms.execute(GetRooms.Params(page, limit), Action {
            val rooms = it.map { RoomViewModel(it, null) }
            rooms.forEach {
                loadLastMessage(it, Action {
                    if (it.lastMessage != null) {
                        view.addOrUpdateRoom(it)
                    }
                })
            }
        })
    }

    override fun listenRoomAdded() {
        listenRoomAdded.execute(null, Action {
            loadLastMessage(RoomViewModel(it), Action {
                if (it.lastMessage != null) {
                    view.addOrUpdateRoom(it)
                }
            })
        })
    }

    override fun listenRoomUpdated() {
        listenRoomUpdated.execute(null, Action {
            loadLastMessage(RoomViewModel(it), Action {
                if (it.lastMessage != null) {
                    view.addOrUpdateRoom(it)
                }
            })
        })
    }

    override fun listenRoomDeleted() {
        listenRoomUpdated.execute(null, Action {
            loadLastMessage(RoomViewModel(it), Action {
                view.removeRoom(it)
            })
        })
    }

    override fun stop() {
        getRoom.dispose()
        getRooms.dispose()
        getMessages.dispose()
        listenNewMessage.dispose()
        listenRoomAdded.dispose()
        listenRoomUpdated.dispose()
        listenRoomDeleted.dispose()
    }
}