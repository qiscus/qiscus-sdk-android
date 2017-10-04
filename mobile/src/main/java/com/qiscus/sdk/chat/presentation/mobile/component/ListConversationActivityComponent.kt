package com.qiscus.sdk.chat.presentation.mobile.component

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory
import com.qiscus.sdk.chat.presentation.listconversation.ListConversationContract
import com.qiscus.sdk.chat.presentation.listconversation.ListConversationPresenter
import com.qiscus.sdk.chat.presentation.mobile.listconversation.ListConversationActivity

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class ListConversationActivityComponent
(
        private val activity: ListConversationActivity,
        private val listConversationView: ListConversationContract.View = activity,
        private val useCaseFactory: QiscusUseCaseFactory = Qiscus.instance.useCaseFactory,

        val listConversationPresenter: ListConversationContract.Presenter = ListConversationPresenter(
                listConversationView,
                useCaseFactory.getRoom(),
                useCaseFactory.getRooms(),
                useCaseFactory.getComments(),
                useCaseFactory.listenNewComment(),
                useCaseFactory.listenRoomAdded(),
                useCaseFactory.listenRoomUpdated(),
                useCaseFactory.listenRoomDeleted()
        )
)