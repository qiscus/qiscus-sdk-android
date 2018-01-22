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

package com.qiscus.sdk.chat.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.util.readDate
import com.qiscus.sdk.chat.domain.util.readEnum
import com.qiscus.sdk.chat.domain.util.writeDate
import com.qiscus.sdk.chat.domain.util.writeEnum
import java.util.*

/**
 * Created on : August 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class Message(
        val messageId: MessageId,
        val text: String,
        val sender: User,
        val date: Date,
        val room: Room,
        var state: MessageState,
        val type: MessageType) : Parcelable {

    private constructor(parcel: Parcel) : this(
            parcel.readParcelable(MessageId::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(User::class.java.classLoader),
            parcel.readDate()!!,
            parcel.readParcelable(Room::class.java.classLoader),
            parcel.readEnum<MessageState>()!!,
            parcel.readParcelable(MessageType::class.java.classLoader))

    override fun toString(): String {
        return "Message(messageId=$messageId, text='$text', sender=$sender, date=$date, " +
                "room=$room, state=$state, type=$type)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false

        if (messageId != other.messageId) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(messageId, flags)
        parcel.writeString(text)
        parcel.writeParcelable(sender, flags)
        parcel.writeDate(date)
        parcel.writeParcelable(room, flags)
        parcel.writeEnum(state)
        parcel.writeParcelable(type, flags)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}