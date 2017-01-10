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

package com.qiscus.sdk.data.local;

import com.qiscus.sdk.data.model.QiscusChatRoom;

import java.util.List;

import rx.Observable;

/**
 * Created on : November 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public interface QiscusChatRoomStore {
    void add(QiscusChatRoom qiscusChatRoom);

    boolean isContains(QiscusChatRoom qiscusChatRoom);

    void update(QiscusChatRoom qiscusChatRoom);

    void addOrUpdate(QiscusChatRoom qiscusChatRoom);

    QiscusChatRoom getChatRoom(int id);

    QiscusChatRoom getChatRoom(String email);

    QiscusChatRoom getChatRoom(String email, String distinctId);

    List<QiscusChatRoom> getChatRooms(int count);

    Observable<List<QiscusChatRoom>> getObservableChatRooms(int count);

    void addRoomMember(int roomId, String email, String distinctId);

    boolean isContainsRoomMember(int roomId, String email);

    List<String> getRoomMembers(int roomId);

    void deleteRoomMember(int roomId, String email);
}
