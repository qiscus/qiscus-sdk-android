package com.qiscus.sdk.chat.domain.executor

import io.reactivex.Scheduler

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface PostExecutionThread {
    val scheduler: Scheduler
}