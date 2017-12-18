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

package com.qiscus.sdk.chat.core.component

import android.content.Context
import com.qiscus.sdk.chat.data.*
import com.qiscus.sdk.chat.data.local.*
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.util.FileUtil
import com.qiscus.sdk.chat.data.local.util.MimeTypeGuesserImpl
import com.qiscus.sdk.chat.data.pubsub.message.MessagePublisher
import com.qiscus.sdk.chat.data.pubsub.message.MessageSubscriber
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.pubsub.file.FileSubscriber
import com.qiscus.sdk.chat.data.pubsub.room.RoomPublisher
import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.data.pubsub.user.UserPublisher
import com.qiscus.sdk.chat.data.pubsub.user.UserSubscriber
import com.qiscus.sdk.chat.data.pusher.*
import com.qiscus.sdk.chat.data.pusher.mapper.MessagePayloadMapper
import com.qiscus.sdk.chat.data.remote.*
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.account.AccountRemote
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import com.qiscus.sdk.chat.data.source.user.UserLocal
import com.qiscus.sdk.chat.data.util.*
import com.qiscus.sdk.chat.domain.pubsub.*
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import com.qiscus.sdk.chat.domain.repository.MessageRepository
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import com.qiscus.sdk.chat.domain.repository.UserRepository
import com.schinizer.rxunfurl.RxUnfurl
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DataComponent(context: Context, applicationWatcher: ApplicationWatcher, restApiServerBaseUrl: String, mqttBrokerUrl: String) {

    private val publishSubject: PublishSubject<Any> by lazy {
        PublishSubject.create<Any>()
    }

    private val okHttpClient: OkHttpClient by lazy {
        HttpClientFactory.makeOkHttpClient(false)
    }

    val dbOpenHelper: DbOpenHelper by lazy {
        DbOpenHelper(context)
    }

    val restApi: RestApi by lazy {
        RestApiFactory.makeQiscusRestApi(restApiServerBaseUrl, okHttpClient)
    }

    val accountLocal: AccountLocal by lazy {
        AccountLocalImpl(context)
    }

    val accountRemote: AccountRemote by lazy {
        AccountRemoteImpl(accountLocal, restApi)
    }

    val accountRepository: AccountRepository by lazy {
        AccountDataRepository(accountLocal, accountRemote)
    }

    val userLocal: UserLocal by lazy {
        UserLocalImpl(dbOpenHelper)
    }

    val userRepository: UserRepository by lazy {
        UserDataRepository(userLocal)
    }

    val mimeTypeGuesser: MimeTypeGuesser by lazy {
        MimeTypeGuesserImpl()
    }

    private val fileUtil: FileUtil by lazy {
        FileUtil(context, mimeTypeGuesser)
    }

    private val filePathGenerator: FilePathGenerator by lazy {
        fileUtil
    }

    private val fileManager: FileManager by lazy {
        fileUtil
    }

    val fileLocal: FileLocal by lazy {
        FileLocalImpl(dbOpenHelper)
    }

    val fileRemote: FileRemote by lazy {
        FileRemoteImpl(accountLocal, restApiServerBaseUrl, okHttpClient, filePathGenerator)
    }

    private val fileDataPusher: FileDataPusher by lazy {
        FileDataPusher(publishSubject)
    }

    val filePublisher: FilePublisher by lazy {
        fileDataPusher
    }

    val fileSubscriber: FileSubscriber by lazy {
        fileDataPusher
    }

    val fileObserver: FileObserver by lazy {
        FileDataObserver(fileSubscriber)
    }

    private val roomDataPusher: RoomDataPusher by lazy {
        RoomDataPusher(publishSubject)
    }

    val roomPublisher: RoomPublisher by lazy {
        roomDataPusher
    }

    val roomSubscriber: RoomSubscriber by lazy {
        roomDataPusher
    }

    val roomLocal: RoomLocal by lazy {
        RoomLocalImpl(dbOpenHelper, accountLocal, userLocal, roomPublisher)
    }

    private val messageDataPusher: MessageDataPusher by lazy {
        MessageDataPusher(publishSubject)
    }

    val messagePublisher: MessagePublisher by lazy {
        messageDataPusher
    }

    val messageSubscriber: MessageSubscriber by lazy {
        messageDataPusher
    }

    val messageLocal: MessageLocal by lazy {
        MessageLocalImpl(dbOpenHelper, accountLocal, roomLocal, userLocal, fileLocal, messagePublisher)
    }

    val roomRemote: RoomRemote by lazy {
        RoomRemoteImpl(accountLocal, restApi, roomLocal, messageLocal)
    }

    val roomRepository: RoomRepository by lazy {
        RoomDataRepository(roomLocal, roomRemote)
    }

    private val userDataPusher: UserDataPusher by lazy {
        UserDataPusher(publishSubject, userLocal)
    }

    val userPublisher: UserPublisher by lazy {
        userDataPusher
    }

    val userSubscriber: UserSubscriber by lazy {
        userDataPusher
    }

    val messageRemote: MessageRemote by lazy {
        MessageRemoteImpl(accountLocal, restApi)
    }

    val postMessageHandler: PostMessageHandler by lazy {
        PostMessageHandlerImpl(messageLocal, messageRemote, fileRemote, filePublisher)
    }

    val messageRepository: MessageRepository by lazy {
        MessageDataRepository(messageLocal, messageRemote, fileLocal, fileRemote, fileManager, filePublisher, postMessageHandler)
    }

    val syncHandler: SyncHandler by lazy {
        SyncHandlerImpl(messageLocal, messageRemote, messageSubscriber)
    }

    internal val messagePayloadMapper: MessagePayloadMapper by lazy {
        MessagePayloadMapper()
    }

    val pubSubClient: PubSubClient by lazy {
        PubSubClientImpl(context, serverUri = mqttBrokerUrl, applicationWatcher = applicationWatcher,
                accountLocal = accountLocal, messageLocal = messageLocal, messagePayloadMapper = messagePayloadMapper,
                postMessageHandler = postMessageHandler, messageRemote = messageRemote, userPublisher = userPublisher,
                syncHandler = syncHandler)
    }

    val roomObserver: RoomObserver by lazy {
        RoomDataObserver(roomSubscriber)
    }

    val messageObserver: MessageObserver by lazy {
        MessageDataObserver(pubSubClient, messageSubscriber)
    }

    val userObserver: UserObserver by lazy {
        UserDataObserver(pubSubClient, userSubscriber)
    }

    val webScrapper: WebScrapper by lazy {
        val rxUnfurl = RxUnfurl.Builder()
                .scheduler(Schedulers.io())
                .build()
        WebScrapperImpl(rxUnfurl)
    }
}