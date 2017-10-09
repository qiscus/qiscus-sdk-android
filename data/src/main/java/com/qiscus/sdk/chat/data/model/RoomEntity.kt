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

package com.qiscus.sdk.chat.data.model

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class RoomEntity @JvmOverloads constructor(
        val id: String,
        val uniqueId: String = "default",
        var name: String,
        var avatar: String = "",
        val group: Boolean = false,
        var options: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (other !is RoomEntity) {
            return false
        }

        return id == other.id && uniqueId == other.uniqueId && name == other.name
                && avatar == other.avatar && options == other.options && group == other.group
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uniqueId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + avatar.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }
}