package com.qiscus.sdk.chat.presentation.uikit.util

import com.qiscus.sdk.chat.domain.model.User
import com.qiscus.sdk.chat.presentation.util.toReadableText
import org.junit.Test

import org.junit.Assert.*

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class TextUtilKtTest {
    @Test
    fun toReadableText() {
        val rawMessage = "halo @[rya.meyvriska@gmail.com] apa kabar?"
        val readableMessage = "halo @Rya Meyvriska apa kabar?"

        val users = HashMap<String, User>()
        users.put("zetra@gmail.com", User("zetra@gmail.com", "Zetra"))
        users.put("rya.meyvriska@gmail.com", User("rya.meyvriska@gmail.com", "Rya Meyvriska"))

        assertEquals(rawMessage.toReadableText(users), readableMessage)
    }

}