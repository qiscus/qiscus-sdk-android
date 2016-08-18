package com.qiscus.library.chat.data.remote.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.library.chat.data.model.ChatRoom;
import com.qiscus.library.chat.util.Qson;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ChatRoomResponse extends Response {

    public ChatRoom getResult() {
        JsonElement chatResponse = data.get("chat_room");
        ChatRoom chatRoom = Qson.pluck().getParser().fromJson(chatResponse, ChatRoom.class);
        JsonObject consultant = chatResponse.getAsJsonObject()
                .get("participants").getAsJsonObject()
                .get("consultants").getAsJsonArray()
                .get(0).getAsJsonObject();
        chatRoom.setRate(consultant.get("rate").getAsDouble());
        chatRoom.setInterlocutorId(consultant.get("id").getAsInt());
        chatRoom.setInterlocutorName(consultant.get("fullname").getAsString());
        chatRoom.setInterlocutorEmail(consultant.get("email").getAsString());
        chatRoom.setInterlocutorAvatar(consultant.get("image").getAsString());
        chatRoom.setInterlocutorGender(consultant.get("gender").getAsString());
        return chatRoom;
    }
}
