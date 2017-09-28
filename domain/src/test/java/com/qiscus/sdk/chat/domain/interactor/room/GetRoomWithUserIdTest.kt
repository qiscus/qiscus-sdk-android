package com.qiscus.sdk.chat.domain.interactor.room

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetRoomWithUserIdTest {
    private lateinit var getRoom: GetRoomWithUserId

    private lateinit var mockRoomRepository: RoomRepository
    private lateinit var mockThreadExecutor: ThreadExecutor
    private lateinit var mockPostExecutionThread: PostExecutionThread

    @Before
    fun setUp() {
        mockRoomRepository = mock()
        mockThreadExecutor = mock()
        mockPostExecutionThread = mock()
        getRoom = GetRoomWithUserId(mockRoomRepository, mockThreadExecutor, mockPostExecutionThread)
    }

    @Test
    fun buildUseCaseObservableCallsRepository() {
        val userId = "1234"
        getRoom.buildUseCaseObservable(GetRoomWithUserId.Params(userId))
        verify(mockRoomRepository).getRoomWithUserId(userId)
    }

    @Test
    fun buildUseCaseObservableCompletes() {
        val userId = "1234"
        stubRoomRepositoryGetRoom(userId)
        val testObserver = getRoom.buildUseCaseObservable(GetRoomWithUserId.Params(userId)).test()
        testObserver.assertComplete()
    }

    @Test
    fun buildUseCaseObservableReturnsData() {
        val userId = "1234"
        stubRoomRepositoryGetRoom(userId)
        val testObserver = getRoom.buildUseCaseObservable(GetRoomWithUserId.Params(userId)).test()
        testObserver.assertValue(Room(1, name = "Room"))
    }

    private fun stubRoomRepositoryGetRoom(userId: String) {
        whenever(mockRoomRepository.getRoomWithUserId(userId))
                .thenReturn(Single.just(Room(1, name = "Room")))
    }
}