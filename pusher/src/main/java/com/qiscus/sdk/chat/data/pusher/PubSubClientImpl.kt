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

package com.qiscus.sdk.chat.data.pusher

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.qiscus.sdk.chat.data.model.AccountEntity
import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.pubsub.user.UserPublisher
import com.qiscus.sdk.chat.data.pusher.mapper.MessagePayloadMapper
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.data.util.ApplicationWatcher
import com.qiscus.sdk.chat.data.util.PostMessageHandler
import com.qiscus.sdk.chat.data.util.SyncHandler
import com.qiscus.sdk.chat.domain.common.runOnBackgroundThread
import com.qiscus.sdk.chat.domain.common.scheduleOnBackgroundThread
import com.qiscus.sdk.chat.domain.pubsub.PubSubClient
import io.reactivex.schedulers.Schedulers
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*
import java.util.concurrent.ScheduledFuture

/**
 * Created on : September 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@SuppressLint("HardwareIds")
class PubSubClientImpl @JvmOverloads constructor(
        private val context: Context,
        private val serverUri: String = "ssl://mqtt.qiscus.com:1885",
        private val clientId: String = "${context.packageName}-" +
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
        private val applicationWatcher: ApplicationWatcher,
        private val postMessageHandler: PostMessageHandler,
        private val syncHandler: SyncHandler,
        private val accountLocal: AccountLocal,
        private val messageLocal: MessageLocal,
        private val messageRemote: MessageRemote,
        private val messagePayloadMapper: MessagePayloadMapper,
        private val userPublisher: UserPublisher)
    : PubSubClient, MqttCallbackExtended, IMqttActionListener {

    private val retryPeriod = 2000L

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var account: AccountEntity

    private val fallbackConnect = Runnable { this.connect() }
    private val fallBackListenMessage = Runnable { this.listenNewMessage() }
    private var fallBackListenMessageState: Runnable? = null
    private var fallBackListenUserStatus: Runnable? = null
    private var fallBackListenUserTyping: Runnable? = null

    private var scheduledConnect: ScheduledFuture<*>? = null
    private var scheduledListenMessage: ScheduledFuture<*>? = null
    private var scheduledListenMessageState: ScheduledFuture<*>? = null
    private var scheduledListenUserStatus: ScheduledFuture<*>? = null
    private var scheduledListenUserTyping: ScheduledFuture<*>? = null

    private var connecting = false
    private var reconnectCounter = 0

    private var scheduledUserStatus: ScheduledFuture<*>? = null
    private var setOfflineCounter = 0

    init {
        buildClient()
    }

    private fun buildClient() {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        mqttAndroidClient.setCallback(this)
        mqttAndroidClient.setTraceEnabled(false)
    }

    override fun connect() {
        if (accountLocal.isAuthenticated() && !connecting) {
            connecting = true
            account = accountLocal.getAccount()
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isAutomaticReconnect = false
            mqttConnectOptions.isCleanSession = false
            mqttConnectOptions.setWill("u/${account.user.id}/s",
                    "0:${Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis}".toByteArray(),
                    2, true)
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, this)
            } catch (e: MqttException) {
                //Do nothing
            } catch (e: NullPointerException) {
                restartConnection()
            } catch (e: IllegalArgumentException) {
                restartConnection()
            }
        }
    }

    override fun isConnected(): Boolean {
        return mqttAndroidClient.isConnected
    }

    override fun restartConnection() {
        try {
            connecting = false
            mqttAndroidClient.disconnect()
            mqttAndroidClient.close()
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }

        clearTasks()

        buildClient()
        connect()
    }

    private fun checkAndConnect() {
        try {
            if (!mqttAndroidClient.isConnected) {
                connect()
            }
        } catch (e: NullPointerException) {
            connect()
        } catch (ignored: Exception) {
            //ignored
        }
    }

    override fun disconnect() {
        setUserStatus(false)
        try {
            connecting = false
            mqttAndroidClient.disconnect()
            mqttAndroidClient.close()
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }

        clearTasks()
        stopUserStatus()
    }

    private fun clearTasks() {
        if (scheduledConnect != null) {
            scheduledConnect!!.cancel(true)
            scheduledConnect = null
        }
        if (scheduledListenMessage != null) {
            scheduledListenMessage!!.cancel(true)
            scheduledListenMessage = null
        }
        if (scheduledListenMessageState != null) {
            scheduledListenMessageState!!.cancel(true)
            scheduledListenMessageState = null
        }
        if (scheduledListenUserStatus != null) {
            scheduledListenUserStatus!!.cancel(true)
            scheduledListenUserStatus = null
        }
        if (scheduledListenUserTyping != null) {
            scheduledListenUserTyping!!.cancel(true)
            scheduledListenUserTyping = null
        }
    }

    override fun listenNewMessage() {
        try {
            mqttAndroidClient.subscribe("${account.token}/c", 2)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            connect()
            scheduledListenMessage = runOnBackgroundThread(fallBackListenMessage, retryPeriod)
        } catch (e: IllegalArgumentException) {
            connect()
            scheduledListenMessage = runOnBackgroundThread(fallBackListenMessage, retryPeriod)
        }
    }

    override fun listenMessageState(roomId: String) {
        fallBackListenMessageState = Runnable { listenMessageState(roomId) }
        try {
            mqttAndroidClient.subscribe("r/$roomId/+/+/d", 2)
            mqttAndroidClient.subscribe("r/$roomId/+/+/r", 2)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            connect()
            scheduledListenMessageState = runOnBackgroundThread(fallBackListenMessageState!!, retryPeriod)
        } catch (e: IllegalArgumentException) {
            connect()
            scheduledListenMessageState = runOnBackgroundThread(fallBackListenMessageState!!, retryPeriod)
        }
    }

    override fun unlistenMessageState(roomId: String) {
        try {
            mqttAndroidClient.unsubscribe("r/$roomId/+/+/d")
            mqttAndroidClient.unsubscribe("r/$roomId/+/+/r")
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }

        if (scheduledListenMessageState != null) {
            scheduledListenMessageState!!.cancel(true)
            scheduledListenMessageState = null
        }

        fallBackListenMessageState = null
    }

    private fun setUserStatus(online: Boolean) {
        checkAndConnect()
        try {
            val message = MqttMessage()
            message.payload = if (online) "1".toByteArray() else "0".toByteArray()
            message.qos = 2
            message.isRetained = true
            mqttAndroidClient.publish("u/" + account.user.id + "/s", message)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }
    }

    private fun scheduleUserStatus() {
        scheduledUserStatus = scheduleOnBackgroundThread(Runnable {
            if (accountLocal.isAuthenticated()) {
                if (applicationWatcher.isOnForeground()) {
                    postMessageHandler.tryResendPendingMessage()
                }
                if (isConnected()) {
                    if (applicationWatcher.isOnForeground()) {
                        setOfflineCounter = 0
                        setUserStatus(true)
                    } else {
                        if (setOfflineCounter <= 2) {
                            setUserStatus(false)
                            setOfflineCounter++
                        }
                    }
                }
            } else {
                stopUserStatus()
            }
        }, 10000)
    }

    private fun stopUserStatus() {
        if (scheduledUserStatus != null) {
            scheduledUserStatus!!.cancel(true)
        }
    }

    override fun listenUserStatus(userId: String) {
        fallBackListenUserStatus = Runnable { listenUserStatus(userId) }
        try {
            mqttAndroidClient.subscribe("u/$userId/s", 2)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            connect()
            scheduledListenUserStatus = runOnBackgroundThread(fallBackListenUserStatus!!, retryPeriod)
        } catch (e: IllegalArgumentException) {
            connect()
            scheduledListenUserStatus = runOnBackgroundThread(fallBackListenUserStatus!!, retryPeriod)
        }
    }

    override fun unlistenUserStatus(userId: String) {
        try {
            mqttAndroidClient.unsubscribe("u/$userId/s")
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }

        if (scheduledListenUserStatus != null) {
            scheduledListenUserStatus!!.cancel(true)
            scheduledListenUserStatus = null
        }
        fallBackListenUserStatus = null
    }

    override fun publishTypingStatus(roomId: String, typing: Boolean) {
        checkAndConnect()
        try {
            val message = MqttMessage()
            message.payload = (if (typing) "1" else "0").toByteArray()
            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/" + account.user.id + "/t", message)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }
    }

    override fun listenUserTyping(roomId: String) {
        fallBackListenUserTyping = Runnable { listenUserTyping(roomId) }
        try {
            mqttAndroidClient.subscribe("r/$roomId/+/+/t", 2)
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            connect()
            scheduledListenUserTyping = runOnBackgroundThread(fallBackListenUserTyping!!, retryPeriod)
        } catch (e: IllegalArgumentException) {
            connect()
            scheduledListenUserTyping = runOnBackgroundThread(fallBackListenUserTyping!!, retryPeriod)
        }
    }

    override fun unlistenUserTyping(roomId: String) {
        try {
            mqttAndroidClient.unsubscribe("r/$roomId/+/+/t")
        } catch (e: MqttException) {
            //Do nothing
        } catch (e: NullPointerException) {
            //Do nothing
        } catch (e: IllegalArgumentException) {
            //Do nothing
        }

        if (scheduledListenUserTyping != null) {
            scheduledListenUserTyping!!.cancel(true)
            scheduledListenUserTyping = null
        }

        fallBackListenUserTyping = null
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        syncHandler.sync()
        try {
            connecting = false
            reconnectCounter = 0
            listenNewMessage()
            if (fallBackListenMessageState != null) {
                scheduledListenMessageState = runOnBackgroundThread(fallBackListenMessageState!!)
            }
            if (fallBackListenUserStatus != null) {
                scheduledListenUserStatus = runOnBackgroundThread(fallBackListenUserStatus!!)
            }
            if (fallBackListenUserTyping != null) {
                scheduledListenUserTyping = runOnBackgroundThread(fallBackListenUserTyping!!)
            }
            if (scheduledConnect != null) {
                scheduledConnect!!.cancel(true)
                scheduledConnect = null
            }
            scheduleUserStatus()
        } catch (ignored: NullPointerException) {
            //Do nothing
        } catch (ignored: IllegalArgumentException) {
            //Do nothing
        }
    }

    override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
        if (topic!!.contains(account.token)) {
            val message = messagePayloadMapper.mapFromPusher(String(mqttMessage!!.payload))
            messageLocal.saveAndNotify(message)
            if (message.sender.id != account.user.id) {
                notifyDelivered(message)
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/t")) {//typing
            val data = topic.split("/")
            if (data[3] != account.user.id) {
                userPublisher.onUserTyping(data[1], data[3], "1" == String(mqttMessage!!.payload))
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/d")) {//delivered
            val data = topic.split("/")
            if (data[3] != account.user.id) {
                val payload = String(mqttMessage!!.payload).split(":")
                messageLocal.updateLastDeliveredMessage(data[1], data[3], MessageIdEntity(payload[0], uniqueId = payload[1]))
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/r")) {//read
            val data = topic.split("/")
            if (data[3] != account.user.id) {
                val payload = String(mqttMessage!!.payload).split(":")
                messageLocal.updateLastReadMessage(data[1], data[3], MessageIdEntity(payload[0], uniqueId = payload[1]))
            }
        } else if (topic.startsWith("u/") && topic.endsWith("/s")) {//online status
            val data = topic.split("/")
            if (data[1] != account.user.id) {
                val payload = String(mqttMessage!!.payload).split(":")
                userPublisher.onUserStatusChanged(data[1], "1" == payload[0], Date(payload[1].substring(0, 13).toLong()))
            }
        }
    }

    private fun notifyDelivered(message: MessageEntity) {
        messageRemote.updateLastDeliveredMessage(message.room.id, message.messageId)
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        //Do nothing
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        //Do nothing
    }

    override fun connectionLost(cause: Throwable?) {
        reconnectCounter++
        connecting = false
        scheduledConnect = runOnBackgroundThread(fallbackConnect, retryPeriod * reconnectCounter)
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        reconnectCounter++
        connecting = false
        scheduledConnect = runOnBackgroundThread(fallbackConnect, retryPeriod * reconnectCounter)
    }
}