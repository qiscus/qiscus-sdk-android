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

package com.qiscus.sdk.chat.data.remote.model

import com.qiscus.sdk.chat.data.model.AccountEntity
import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

data class AccountResponseModel(var results: Results, var status: Int) {

    data class Results(var user: User)

    data class User(
            var avatarUrl: String,
            var email: String,
            var id: Long,
            var lastCommentId: Long,
            var pnAndroidConfigured: Boolean,
            var pnIosConfigured: Boolean,
            var token: String,
            var username: String
    ) {

        fun toEntity(): AccountEntity {
            return AccountEntity(UserEntity(email, username, avatarUrl), token)
        }
    }
}