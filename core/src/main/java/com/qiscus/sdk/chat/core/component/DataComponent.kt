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
import com.qiscus.sdk.chat.data.pubsub.comment.CommentPublisher
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.pubsub.file.FileSubscriber
import com.qiscus.sdk.chat.data.pubsub.room.RoomPublisher
import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.data.pubsub.user.UserPublisher
import com.qiscus.sdk.chat.data.pubsub.user.UserSubscriber
import com.qiscus.sdk.chat.data.pusher.*
import com.qiscus.sdk.chat.data.pusher.mapper.CommentPayloadMapper
import com.qiscus.sdk.chat.data.remote.*
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.account.AccountRemote
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.file.FileRemote
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import com.qiscus.sdk.chat.data.source.user.UserLocal
import com.qiscus.sdk.chat.data.util.*
import com.qiscus.sdk.chat.domain.pubsub.*
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import com.qiscus.sdk.chat.domain.repository.CommentRepository
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

    val qiscusRestApi: QiscusRestApi by lazy {
        QiscusRestApiFactory.makeQiscusRestApi(restApiServerBaseUrl, okHttpClient)
    }

    val accountLocal: AccountLocal by lazy {
        AccountLocalImpl(context)
    }

    val accountRemote: AccountRemote by lazy {
        AccountRemoteImpl(accountLocal, qiscusRestApi)
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

    private val commentDataPusher: CommentDataPusher by lazy {
        CommentDataPusher(publishSubject)
    }

    val commentPublisher: CommentPublisher by lazy {
        commentDataPusher
    }

    val commentSubscriber: CommentSubscriber by lazy {
        commentDataPusher
    }

    val commentLocal: CommentLocal by lazy {
        CommentLocalImpl(dbOpenHelper, accountLocal, roomLocal, userLocal, fileLocal, commentPublisher)
    }

    val roomRemote: RoomRemote by lazy {
        RoomRemoteImpl(accountLocal, qiscusRestApi, roomLocal, commentLocal)
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

    val commentRemote: CommentRemote by lazy {
        CommentRemoteImpl(accountLocal, qiscusRestApi)
    }

    val postCommentHandler: PostCommentHandler by lazy {
        PostCommentHandlerImpl(commentLocal, commentRemote, fileRemote, filePublisher)
    }

    val commentRepository: CommentRepository by lazy {
        CommentDataRepository(commentLocal, commentRemote, fileLocal, fileRemote, fileManager, filePublisher, postCommentHandler)
    }

    val syncHandler: SyncHandler by lazy {
        SyncHandlerImpl(commentLocal, commentRemote, commentSubscriber)
    }

    internal val commentPayloadMapper: CommentPayloadMapper by lazy {
        CommentPayloadMapper()
    }

    val pubSubClient: QiscusPubSubClient by lazy {
        QiscusMqttClient(context, serverUri = mqttBrokerUrl, applicationWatcher = applicationWatcher,
                accountLocal = accountLocal, commentLocal = commentLocal, commentPayloadMapper = commentPayloadMapper,
                postCommentHandler = postCommentHandler, commentRemote = commentRemote, userPublisher = userPublisher,
                syncHandler = syncHandler)
    }

    val roomObserver: RoomObserver by lazy {
        RoomDataObserver(roomSubscriber)
    }

    val commentObserver: CommentObserver by lazy {
        CommentDataObserver(pubSubClient, commentSubscriber)
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