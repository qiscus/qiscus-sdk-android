package com.qiscus.sdk.chat.presentation.mobile.listconversation

import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory
import com.qiscus.sdk.chat.presentation.listconversation.ListConversationContract
import com.qiscus.sdk.chat.presentation.listconversation.ListConversationPresenter
import com.qiscus.sdk.chat.presentation.mobile.R

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
        private @ColorInt val mentionAllColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_all),
        private @ColorInt val mentionOtherColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_other),
        private @ColorInt val mentionMeColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_me),

        val listConversationPresenter: ListConversationContract.Presenter = ListConversationPresenter(
                listConversationView,
                useCaseFactory.getRoom(),
                useCaseFactory.getRooms(),
                useCaseFactory.getComments(),
                useCaseFactory.listenNewComment(),
                useCaseFactory.listenRoomAdded(),
                useCaseFactory.listenRoomUpdated(),
                useCaseFactory.listenRoomDeleted(),
                mentionAllColor,
                mentionOtherColor,
                mentionMeColor
        )
)