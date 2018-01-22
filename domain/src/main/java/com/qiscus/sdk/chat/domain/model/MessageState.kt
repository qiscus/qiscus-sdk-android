package com.qiscus.sdk.chat.domain.model

/**
 * Created on : January 22, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
enum class MessageState(val intValue: Int) {
    PENDING(0), SENDING(1), ON_SERVER(2), DELIVERED(3), READ(4), FAILED(-1);

    companion object {
        fun valueOf(intValue: Int): MessageState {
            return when (intValue) {
                0 -> PENDING
                1 -> SENDING
                2 -> ON_SERVER
                3 -> DELIVERED
                4 -> READ
                else -> FAILED
            }
        }
    }
}