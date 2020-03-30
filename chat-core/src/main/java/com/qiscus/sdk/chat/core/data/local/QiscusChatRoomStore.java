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

import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QParticipant;

import java.util.List;

import rx.Observable;

/**
 * Created on : November 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface QiscusChatRoomStore {
    void add(QChatRoom qiscusChatRoom);

    boolean isContains(QChatRoom qiscusChatRoom);

    void update(QChatRoom qiscusChatRoom);

    void addOrUpdate(QChatRoom qiscusChatRoom);

    QChatRoom getChatRoom(long roomId);

    QChatRoom getChatRoom(String email);

    QChatRoom getChatRoomWithUniqueId(String uniqueId);

    List<QChatRoom> getChatRooms(int limit);

    List<QChatRoom> getChatRooms(int limit, int offset);

    Observable<List<QChatRoom>> getObservableChatRooms(int limit);

    Observable<List<QChatRoom>> getObservableChatRooms(int limit, int offset);

    List<QChatRoom> getChatRooms(List<Long> roomIds, List<String> uniqueIds);

    void deleteChatRoom(long roomId);

    void addRoomMember(long roomId, QParticipant qiscusRoomMember);

    boolean isContainsRoomMember(long roomId, String email);

    void updateRoomMember(long roomId, QParticipant qiscusRoomMember);

    void addOrUpdateRoomMember(long roomId, QParticipant qiscusRoomMember);

    List<QParticipant> getRoomMembers(long roomId);

    void deleteRoomMember(long roomId, String email);

    void deleteRoomMembers(long roomId);
}
