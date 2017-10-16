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

package com.qiscus.sdk.chat.domain.common

import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created on : August 18, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
private val random = Random()
private val symbols = '0'.rangeTo('9').toMutableList().apply {
    addAll('a'.rangeTo('z'))
    addAll('A'.rangeTo('Z'))
    toList()
}

private val taskExecutor = ScheduledThreadPoolExecutor(5)

fun randomInt(n: Int): Int {
    return random.nextInt(n)
}

@JvmOverloads
fun randomString(length: Int = 64): String {
    val buf = CharArray(length)
    for (i in buf.indices) {
        buf[i] = symbols[randomInt(symbols.size)]
    }
    return String(buf)
}

fun generateUniqueId(): String {
    return "${System.currentTimeMillis()}_${randomString()}"
}

@JvmOverloads
fun runOnBackgroundThread(runnable: Runnable, delay: Long = 0): ScheduledFuture<*> {
    return taskExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS)
}

fun scheduleOnBackgroundThread(runnable: Runnable, delay: Long): ScheduledFuture<*> {
    return taskExecutor.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MILLISECONDS)
}