package com.qiscus.sdk.chat.presentation.model

import android.os.Parcel
import android.os.Parcelable
import com.qiscus.sdk.chat.domain.model.Room

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class RoomViewModel @JvmOverloads constructor(var room: Room, var lastMessage: MessageViewModel? = null): Parcelable {

    private constructor(parcel: Parcel) :
            this(parcel.readParcelable(Room::class.java.classLoader), parcel.readParcelable(MessageViewModel::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(room, flags)
        parcel.writeParcelable(lastMessage, flags)
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    companion object CREATOR : Parcelable.Creator<RoomViewModel> {
        override fun createFromParcel(parcel: Parcel): RoomViewModel {
            return RoomViewModel(parcel)
        }

        override fun newArray(size: Int): Array<RoomViewModel?> {
            return arrayOfNulls(size)
        }
    }
}