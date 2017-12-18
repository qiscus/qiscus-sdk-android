package com.qiscus.sdk.chat.presentation.mobile.listroom

import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory
import com.qiscus.sdk.chat.presentation.listroom.ListRoomContract
import com.qiscus.sdk.chat.presentation.listroom.ListRoomPresenter
import com.qiscus.sdk.chat.presentation.mobile.R

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
        private val useCaseFactory: QiscusUseCaseFactory = Qiscus.instance.useCaseFactory,
        private @ColorInt val mentionAllColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_all),
        private @ColorInt val mentionOtherColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_other),
        private @ColorInt val mentionMeColor: Int = ContextCompat.getColor(activity, R.color.qiscus_mention_me),

        val listRoomPresenter: ListRoomContract.Presenter = ListRoomPresenter(
                listRoomView,
                useCaseFactory.getRoom(),
                useCaseFactory.getRooms(),
                useCaseFactory.getMessages(),
                useCaseFactory.listenNewMessage(),
                useCaseFactory.listenRoomAdded(),
                useCaseFactory.listenRoomUpdated(),
                useCaseFactory.listenRoomDeleted(),
                mentionAllColor,
                mentionOtherColor,
                mentionMeColor
        )
)