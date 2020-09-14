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

package com.qiscus.sdk.chat.core.data.local;

import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.List;

import io.reactivex.Observable;;

/**
 * Created on : November 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface QiscusChatRoomStore {
    void add(QiscusChatRoom qiscusChatRoom);

    boolean isContains(QiscusChatRoom qiscusChatRoom);

    void update(QiscusChatRoom qiscusChatRoom);

    void addOrUpdate(QiscusChatRoom qiscusChatRoom);

    QiscusChatRoom getChatRoom(long roomId);

    QiscusChatRoom getChatRoom(String email);

    QiscusChatRoom getChatRoom(String email, String distinctId);

    QiscusChatRoom getChatRoomWithUniqueId(String uniqueId);

    List<QiscusChatRoom> getChatRooms(int limit);

    List<QiscusChatRoom> getChatRooms(int limit, int offset);

    Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit);

    Observable<List<QiscusChatRoom>> getObservableChatRooms(int limit, int offset);

    List<QiscusChatRoom> getChatRooms(List<Long> roomIds, List<String> uniqueIds);

    void deleteChatRoom(long roomId);

    void addRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId);

    boolean isContainsRoomMember(long roomId, String email);

    void updateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId);

    void addOrUpdateRoomMember(long roomId, QiscusRoomMember qiscusRoomMember, String distinctId);

    List<QiscusRoomMember> getRoomMembers(long roomId);

    void deleteRoomMember(long roomId, String email);

    void deleteRoomMembers(long roomId);
}
