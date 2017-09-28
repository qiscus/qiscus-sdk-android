package com.qiscus.sdk.chat.domain.common

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UtilsKtTest {
    @Test
    fun randomStringTest() {
        assertNotNull(randomString())
    }

    @Test
    fun generateUniqueIdTest() {
        assertNotNull(generateUniqueId())
    }
}