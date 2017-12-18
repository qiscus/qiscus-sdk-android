package com.qiscus.sdk.chat.presentation.listroom

import com.qiscus.sdk.chat.presentation.BasePresenter
import com.qiscus.sdk.chat.presentation.BaseView
import com.qiscus.sdk.chat.presentation.model.RoomViewModel

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ListRoomContract {
    interface View : BaseView {
        fun addOrUpdateRoom(roomViewModel: RoomViewModel)

        fun removeRoom(roomViewModel: RoomViewModel)
    }

    interface Presenter : BasePresenter {
        fun loadRooms(page: Int = 1, limit: Int = 20)

        fun listenRoomAdded()

        fun listenRoomUpdated()

        fun listenRoomDeleted()
    }
}