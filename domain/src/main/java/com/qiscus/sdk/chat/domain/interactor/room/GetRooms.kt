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
class GetRooms(private val roomRepository: RoomRepository,
               threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<List<Room>, GetRooms.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Single<List<Room>> {
        return roomRepository.getRooms(params!!.page, params.limit)
    }

    data class Params(val page: Int, val limit: Int)
}