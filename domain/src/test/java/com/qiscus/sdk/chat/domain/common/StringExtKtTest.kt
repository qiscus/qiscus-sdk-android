package com.qiscus.sdk.chat.domain.common

import org.junit.Test

import org.junit.Assert.*

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class StringExtKtTest {
    @Test
    fun isUrl() {
        assertTrue("http://www.google.com".isUrl())
        assertTrue("www.google.com".isUrl())
        assertTrue("google.com".isUrl())
        assertTrue("http://www.google.co.id".isUrl())
        assertTrue("www.google.co.id".isUrl())
        assertTrue("google.co.id".isUrl())

        assertFalse("".isUrl())
        assertFalse("asasas".isUrl())
        assertFalse("http:// www.google.com".isUrl())
        assertFalse("contact@google.com".isUrl())
    }

    @Test
    fun containsUrl() {
        assertTrue("http://www.google.com".containsUrl())
        assertTrue("www.google.com".containsUrl())
        assertTrue("google.com".containsUrl())
        assertTrue("http://www.google.co.id".containsUrl())
        assertTrue("www.google.co.id".containsUrl())
        assertTrue("google.co.id".containsUrl())
        assertTrue("http:// www.google.com".containsUrl())

        assertFalse("".containsUrl())
        assertFalse("asasas".containsUrl())
        assertFalse("contact@google.com".containsUrl())
    }

    @Test
    fun extractUrls() {
        assertEquals(3, "http://www.google.com www.google.com google.com".extractUrls().size)
        assertEquals(3, "http://www.google.co.id www.google.co.id google.co.id".extractUrls().size)
        assertEquals(1, " asasas http:// http:// www.google.com contact@google.com".extractUrls().size)
    }
}