package com.qiscus.sdk.chat.data.model

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class RoomEntity(
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