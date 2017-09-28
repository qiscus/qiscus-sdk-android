package com.qiscus.sdk.chat.domain.interactor.room

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import io.reactivex.Single

/**
 * Created on : September 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetRoomWithChannelId(private val roomRepository: RoomRepository,
                           threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<Room, GetRoomWithChannelId.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<Room> {
        return roomRepository.getRoomWithChannelId(params!!.channelId, params.roomAvatarUrl)
    }

    data class Params(val channelId: String, val roomAvatarUrl: String) {
        constructor(channelId: String) : this(channelId, "")
    }
}