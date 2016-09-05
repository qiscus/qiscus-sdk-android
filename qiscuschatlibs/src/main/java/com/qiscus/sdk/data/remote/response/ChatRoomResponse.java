package com.qiscus.sdk.data.remote.response;

import com.google.gson.JsonElement;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.util.Qson;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ChatRoomResponse extends Response {

    public QiscusChatRoom getResult() {
        JsonElement chatResponse = data.get("chat_room");
        return Qson.pluck().getParser().fromJson(chatResponse, QiscusChatRoom.class);
    }
}
