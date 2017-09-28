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