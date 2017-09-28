package com.qiscus.sdk.chat.data.local

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.qiscus.sdk.chat.data.model.AccountEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AccountLocalImpl(context: Context) : AccountLocal {

    private val sharedPreferences = context.getSharedPreferences("qiscus_account", Context.MODE_PRIVATE)
    private val gson = makeGson()

    override fun saveAccount(accountEntity: AccountEntity) {
        sharedPreferences.edit().putString("account", gson.toJson(accountEntity)).apply()
    }

    override fun getAccount(): AccountEntity {
        if (!sharedPreferences.contains("account")) {
            throw RuntimeException("You are not logged in!")
        }
        return gson.fromJson(sharedPreferences.getString("account", ""), AccountEntity::class.java)
    }

    override fun isAuthenticate(): Boolean {
        return sharedPreferences.contains("account")
    }

    override fun clearData() {
        sharedPreferences.edit().clear().apply()
    }

    private fun makeGson(): Gson {
        return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
    }
}