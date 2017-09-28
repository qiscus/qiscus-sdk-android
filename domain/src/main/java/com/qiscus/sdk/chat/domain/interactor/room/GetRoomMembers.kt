package com.qiscus.sdk.chat.domain.interactor.room

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.RoomMember
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import io.reactivex.Single

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetRoomMembers(private val roomRepository: RoomRepository,
                     threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<List<RoomMember>, GetRoomMembers.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<List<RoomMember>> {
        return roomRepository.getRoomMembers(params!!.roomId)
    }

    data class Params(val roomId: String)
}