package com.qiscus.sdk.chat.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.account.AuthenticateWithKey
import com.qiscus.sdk.chat.domain.interactor.room.GetRoomWithUserId
import com.qiscus.sdk.chat.domain.interactor.room.GetRooms
import com.qiscus.sdk.chat.sample.chat.chatIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val useCaseFactory = Qiscus.instance.useCaseFactory

    private val getRooms = useCaseFactory.getRooms()
    private val listenRoomAdded = useCaseFactory.listenRoomAdded()
    private val listenRoomUpdated = useCaseFactory.listenRoomUpdated()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accountRepository = Qiscus.instance.component.dataComponent.accountRepository

        loginButton.text = if (accountRepository.isAuthenticated().blockingGet()) "Logout" else "Login"

        loginButton.setOnClickListener {
            if (accountRepository.isAuthenticated().blockingGet()) {
                logout()
            } else {
                authenticate()
            }
        }

        startChatButton.setOnClickListener { openChatWith("rya.meyvriska244@gmail.com") }

        startChatButton.setOnLongClickListener {
            loadRooms(1)
            true
        }

        listenRoomAdded.execute(null, Action { Log.d("ZETRA", "Room ${it.name} added") })

        listenRoomUpdated.execute(null, Action { Log.d("ZETRA", "Room ${it.name} updated") })
    }

    private fun loadRooms(page: Int) {
        Log.d("ZETRA", "loadRooms")
        getRooms.execute(GetRooms.Params(page, 20), Action {
            if (it.isNotEmpty()) {
                loadRooms(page + 1)
            }
        })
    }

    private fun authenticate() {
        val authenticate = useCaseFactory.authenticateWithKey()

        authenticate.execute(AuthenticateWithKey.Params("zetra255@gmail.com", "12345678", "Zetra"),
                Action {
                    Log.d("ZETRA", "Login with: $it")
                    loginButton.text = "Logout"
                }, Action { it.printStackTrace() })
    }

    private fun logout() {
        val logout = useCaseFactory.logout()
        logout.execute(null, Action { loginButton.text = "Login" }, Action { it.printStackTrace() })
    }

    private fun openChatWith(userId: String) {
        val getRoom = useCaseFactory.getRoomWithUserId()

        getRoom.execute(GetRoomWithUserId.Params(userId), Action { startActivity(chatIntent(it)) })
    }

    override fun onDestroy() {
        super.onDestroy()
        listenRoomAdded.dispose()
        listenRoomUpdated.dispose()
    }
}
