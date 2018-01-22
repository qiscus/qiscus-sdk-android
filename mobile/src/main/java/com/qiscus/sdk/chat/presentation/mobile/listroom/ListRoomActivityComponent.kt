package com.qiscus.sdk.chat.presentation.mobile.listroom

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.UseCaseFactory
import com.qiscus.sdk.chat.presentation.listroom.ListRoomContract
import com.qiscus.sdk.chat.presentation.listroom.ListRoomPresenter

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class ListRoomActivityComponent
(
        private val activity: ListRoomActivity,
        private val listRoomView: ListRoomContract.View = activity,
        private val useCaseFactory: UseCaseFactory = Qiscus.instance.useCaseFactory,

        val listRoomPresenter: ListRoomContract.Presenter = ListRoomPresenter(
                listRoomView,
                useCaseFactory.getRoom(),
                useCaseFactory.getRooms(),
                useCaseFactory.getMessages(),
                useCaseFactory.listenNewMessage(),
                useCaseFactory.listenRoomAdded(),
                useCaseFactory.listenRoomUpdated(),
                useCaseFactory.listenRoomDeleted()
        )
)