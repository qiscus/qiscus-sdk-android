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
class CreateGroupRoom(private val roomRepository: RoomRepository,
              threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<Room, CreateGroupRoom.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<Room> {
        return roomRepository.createGroupRoom(params!!.userIds, params.roomName, params.roomAvatarUrl)
    }

    data class Params(val userIds: List<String>, val roomName: String, val roomAvatarUrl: String)
}