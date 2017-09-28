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
class GetRoomsWithSpecificIds(private val roomRepository: RoomRepository,
                              threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<List<Room>, GetRoomsWithSpecificIds.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Single<List<Room>> {
        return roomRepository.getRoomsWithSpecificIds(params!!.roomIds, params.channelIds)
    }

    data class Params(val roomIds: List<String>, val channelIds: List<String>)
}