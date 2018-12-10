/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.interactor.Action
import com.qiscus.sdk.chat.domain.interactor.account.AuthenticateWithKey
import com.qiscus.sdk.chat.domain.interactor.room.GetRoomWithUserId
import com.qiscus.sdk.chat.domain.interactor.room.GetRooms
import com.qiscus.sdk.chat.presentation.mobile.listroom.ListRoomActivity
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

        loginButton.text = if (Qiscus.instance.isAuthenticated()) "Logout" else "Login"

        loginButton.setOnClickListener {
            if (Qiscus.instance.isAuthenticated()) {
                logout()
            } else {
                authenticate()
            }
        }

        startChatButton.setOnClickListener {
            if (Qiscus.instance.isAuthenticated()) {
                startActivity(Intent(this, ListRoomActivity::class.java))
            } else {
                Toast.makeText(this, "You must login first", Toast.LENGTH_SHORT).show()
            }
        }

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

        authenticate.execute(AuthenticateWithKey.Params("zetra25@gmail.com", "12345678", "Zetra"),
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
