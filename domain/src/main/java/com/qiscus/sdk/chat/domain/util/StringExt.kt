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

package com.qiscus.sdk.chat.domain.util

import java.net.URLDecoder

/**
 * Created on : August 18, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun String.isUrl(): Boolean {
    return Patterns.AUTOLINK_WEB_URL.matcher(this).matches()
}

fun String.containsUrl(): Boolean {
    val matcher = Patterns.AUTOLINK_WEB_URL.matcher(this)
    val contains = matcher.find()
    if (contains) {
        val start = matcher.start()
        if (start == 0 || (start > 0 && this[start - 1] != '@')) {
            return true
        }
        return false
    }
    return contains
}

fun String.extractUrls(): List<String> {
    val urls = arrayListOf<String>()
    val matcher = Patterns.AUTOLINK_WEB_URL.matcher(this)
    while (matcher.find()) {
        val start = matcher.start()
        if (start > 0 && this[start - 1] == '@') {
            continue
        }
        val end = matcher.end()
        if (end < length && start > 0 && this[start - 1] == '@') {
            continue
        }

        var url = matcher.group()
        if (!url.startsWith("http")) {
            url = "http://" + url
        }
        urls.add(url)
    }
    return urls
}

fun String.getAttachmentName(): String {
    val url = URLDecoder.decode(this, "UTF-8")
    val lastPathPos = url.lastIndexOf('/') + 1
    if (lastPathPos >= 0 && lastPathPos < url.length) {
        return url.substring(lastPathPos)
    }
    return url
}

fun String.getExtension(): String {
    val lastDotPosition = lastIndexOf('.')
    if (lastDotPosition == -1 || lastDotPosition == length - 1) {
        return ""
    }

    val ext = substring(lastDotPosition + 1)
    return ext.trim().toLowerCase()
}