package com.qiscus.sdk.chat.presentation.mobile.listroom

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.mobile.chatroom.chatRoomIntent
import com.qiscus.sdk.chat.presentation.model.RoomViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.SortedAdapter
import com.qiscus.sdk.chat.presentation.util.indexOfFirst
import com.qiscus.sdk.chat.presentation.util.toTodayOrDate

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomAdapter(private val context: Context) : SortedAdapter<RoomViewModel, RoomAdapter.VH>() {

    fun addOrUpdate(roomViewModel: RoomViewModel) {
        val pos = data.indexOfFirst { it.room.id == roomViewModel.room.id }
        if (pos >= 0) {
            data.updateItemAt(pos, roomViewModel)
        } else {
            data.add(roomViewModel)
        }
        notifyDataSetChanged()
    }

    fun removeRoom(roomViewModel: RoomViewModel) {
        val pos = data.indexOfFirst { it.room.id == roomViewModel.room.id }
        if (pos >= 0) {
            data.removeItemAt(pos)
        }
        notifyDataSetChanged()
    }

    override fun getItemClass(): Class<RoomViewModel> {
        return RoomViewModel::class.java
    }

    override fun compare(lhs: RoomViewModel, rhs: RoomViewModel): Int {
        return if (lhs.lastMessage == null) {
            1
        } else if (rhs.lastMessage == null) {
            -1
        } else if (lhs.lastMessage == null && rhs.lastMessage == null) {
            0
        } else {
            rhs.lastMessage!!.message.date.compareTo(lhs.lastMessage!!.message.date)
        }
    }

    override fun areContentsTheSame(oldE: RoomViewModel, newE: RoomViewModel): Boolean {
        return oldE.room == newE.room
    }

    override fun areItemsTheSame(oldE: RoomViewModel, newE: RoomViewModel): Boolean {
        return oldE.room == newE.room
    }

    override fun getItemCount(): Int {
        return data.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_qiscus_room, parent, false))
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder!!.bind(data[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val roomAvatarView: ImageView = view.findViewById(R.id.roomAvatar)
        private val roomNameView: TextView = view.findViewById(R.id.roomName)
        private val lastMessageView: TextView = view.findViewById(R.id.lastMessage)
        private val lastMessageDateView: TextView = view.findViewById(R.id.lastMessageDate)
        private val unreadCountView: TextView = view.findViewById(R.id.unreadCount)

        private val normalColor = ContextCompat.getColor(view.context, R.color.qiscus_list_room_last_message_date)
        private val unreadColor = ContextCompat.getColor(view.context, R.color.qiscus_list_room_last_message_date_active)

        fun bind(roomViewModel: RoomViewModel) {
            Glide.with(roomAvatarView).load(roomViewModel.room.avatar).into(roomAvatarView)
            roomNameView.text = roomViewModel.room.name
            lastMessageView.text = roomViewModel.lastMessage?.spannableMessage
            lastMessageDateView.text = roomViewModel.lastMessage?.message?.date?.toTodayOrDate()
            unreadCountView.text = "${roomViewModel.room.unreadCount}"

            if (roomViewModel.room.unreadCount > 0) {
                unreadCountView.visibility = View.VISIBLE
                lastMessageDateView.setTextColor(unreadColor)
            } else {
                unreadCountView.visibility = View.GONE
                lastMessageDateView.setTextColor(normalColor)
            }

            roomAvatarView.setOnClickListener {
                it.context.startActivity(it.context.chatRoomIntent(roomViewModel.room))
            }
        }
    }
}