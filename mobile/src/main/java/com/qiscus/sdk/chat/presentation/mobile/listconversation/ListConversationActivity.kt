package com.qiscus.sdk.chat.presentation.mobile.listconversation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.qiscus.sdk.chat.presentation.listconversation.ListConversationContract
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.mobile.component.ListConversationActivityComponent
import com.qiscus.sdk.chat.presentation.model.ConversationView
import kotlinx.android.synthetic.main.activity_list_conversation.*

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListConversationActivity : AppCompatActivity(), ListConversationContract.View {
    private lateinit var listConversationPresenter: ListConversationContract.Presenter

    private val adapter = ConversationAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_conversation)
        init()

        conversationRecyclerView.adapter = adapter
        conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        conversationRecyclerView.setHasFixedSize(true)
    }

    private fun init() {
        val activityComponent = ListConversationActivityComponent(this)
        listConversationPresenter = activityComponent.listConversationPresenter
    }

    override fun onStart() {
        super.onStart()
        listConversationPresenter.start()
    }

    override fun addOrUpdateConversation(conversationView: ConversationView) {
        adapter.addOrUpdate(conversationView)
    }

    override fun removeConversation(conversationView: ConversationView) {
        adapter.removeConversation(conversationView)
    }

    override fun onStop() {
        super.onStop()
        listConversationPresenter.stop()
    }
}