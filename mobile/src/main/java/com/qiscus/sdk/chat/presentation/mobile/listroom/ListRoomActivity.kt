package com.qiscus.sdk.chat.presentation.mobile.listroom

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.qiscus.sdk.chat.presentation.listroom.ListRoomContract
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.RoomViewModel
import kotlinx.android.synthetic.main.activity_list_room.*

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListRoomActivity : AppCompatActivity(), ListRoomContract.View {
    private lateinit var listRoomPresenter: ListRoomContract.Presenter

    private val adapter = RoomAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_room)

        roomRecyclerView.adapter = adapter
        roomRecyclerView.layoutManager = LinearLayoutManager(this)
        roomRecyclerView.setHasFixedSize(true)
    }

    private fun init() {
        val activityComponent = ListRoomActivityComponent(this)
        listRoomPresenter = activityComponent.listRoomPresenter
        listRoomPresenter.start()
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    override fun addOrUpdateRoom(roomViewModel: RoomViewModel) {
        adapter.addOrUpdate(roomViewModel)
    }

    override fun removeRoom(roomViewModel: RoomViewModel) {
        adapter.removeRoom(roomViewModel)
    }

    override fun onStop() {
        super.onStop()
        listRoomPresenter.stop()
    }
}