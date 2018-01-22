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
import com.qiscus.sdk.chat.domain.util.readDate
import com.qiscus.sdk.chat.domain.util.writeBoolean
import com.qiscus.sdk.chat.domain.util.writeDate
import java.util.*

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class UserStatus(val user: User, val online: Boolean, val lastActive: Date) : Parcelable {

    private constructor(parcel: Parcel) :
            this(parcel.readParcelable(User::class.java.classLoader), parcel.readBoolean(), parcel.readDate()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(user, flags)
        parcel.writeBoolean(online)
        parcel.writeDate(lastActive)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<UserStatus> {
        override fun createFromParcel(parcel: Parcel): UserStatus {
            return UserStatus(parcel)
        }

        override fun newArray(size: Int): Array<UserStatus?> {
            return arrayOfNulls(size)
        }
    }
}