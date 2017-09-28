package com.qiscus.sdk.chat.domain.interactor.room

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetRoomWithUserId(private val roomRepository: RoomRepository,
                        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<Room, GetRoomWithUserId.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<Room> {
        return roomRepository.getRoomWithUserId(params!!.userId)
    }

    data class Params(val userId: String)
}