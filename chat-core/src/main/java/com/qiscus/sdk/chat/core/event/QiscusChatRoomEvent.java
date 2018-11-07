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

package com.qiscus.sdk.chat.core.event;

import org.json.JSONObject;

/**
 * Created on : October 31, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChatRoomEvent {
    private long roomId;
    private long commentId;
    private String commentUniqueId;
    private boolean typing;
    private String user;
    private Event event;
    private JSONObject eventData;

    public long getRoomId() {
        return roomId;
    }

    public QiscusChatRoomEvent setRoomId(long roomId) {
        this.roomId = roomId;
        return this;
    }

    public long getCommentId() {
        return event == Event.TYPING ? -1 : commentId;
    }

    public QiscusChatRoomEvent setCommentId(long commentId) {
        this.commentId = commentId;
        return this;
    }

    public String getCommentUniqueId() {
        return commentUniqueId;
    }

    public QiscusChatRoomEvent setCommentUniqueId(String commentUniqueId) {
        this.commentUniqueId = commentUniqueId;
        return this;
    }

    public boolean isTyping() {
        return typing;
    }

    public QiscusChatRoomEvent setTyping(boolean typing) {
        this.typing = typing;
        return this;
    }

    public String getUser() {
        return user;
    }

    public QiscusChatRoomEvent setUser(String user) {
        this.user = user;
        return this;
    }

    public Event getEvent() {
        return event;
    }

    public QiscusChatRoomEvent setEvent(Event event) {
        this.event = event;
        return this;
    }

    public JSONObject getEventData() {
        return eventData;
    }

    public QiscusChatRoomEvent setEventData(JSONObject data) {
        this.eventData = data;
        return this;
    }

    @Override
    public String toString() {
        return "QiscusChatRoomEvent{" +
                "roomId=" + roomId +
                ", commentId=" + commentId +
                ", commentUniqueId='" + commentUniqueId + '\'' +
                ", typing=" + typing +
                ", user='" + user + '\'' +
                ", event=" + event +
                ", eventData=" + eventData +
                '}';
    }

    public enum Event {
        TYPING, DELIVERED, READ, CUSTOM
    }
}
