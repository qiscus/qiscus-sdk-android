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
import com.qiscus.sdk.chat.domain.util.readBoolean
import com.qiscus.sdk.chat.domain.util.writeBoolean

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class UserTyping(val roomId: String, val user: User, val typing: Boolean) : Parcelable {

    private constructor(parcel: Parcel) :
            this(parcel.readString(), parcel.readParcelable(User::class.java.classLoader), parcel.readBoolean())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeParcelable(user, flags)
        parcel.writeBoolean(typing)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<UserTyping> {
        override fun createFromParcel(parcel: Parcel): UserTyping {
            return UserTyping(parcel)
        }

        override fun newArray(size: Int): Array<UserTyping?> {
            return arrayOfNulls(size)
        }
    }
}