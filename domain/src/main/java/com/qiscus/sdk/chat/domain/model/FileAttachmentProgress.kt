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
import com.qiscus.sdk.chat.domain.util.readEnum
import com.qiscus.sdk.chat.domain.util.writeEnum

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class FileAttachmentProgress(val fileAttachmentMessage: FileAttachmentMessage, val state: State, var progress: Int) : Parcelable {

    private constructor(parcel: Parcel) : this(
            parcel.readParcelable(FileAttachmentMessage::class.java.classLoader),
            parcel.readEnum<State>()!!,
            parcel.readInt())

    enum class State {
        UPLOADING,
        DOWNLOADING
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(fileAttachmentMessage, flags)
        parcel.writeEnum(state)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<FileAttachmentProgress> {
        override fun createFromParcel(parcel: Parcel): FileAttachmentProgress {
            return FileAttachmentProgress(parcel)
        }

        override fun newArray(size: Int): Array<FileAttachmentProgress?> {
            return arrayOfNulls(size)
        }
    }
}