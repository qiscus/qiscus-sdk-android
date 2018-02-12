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

package com.qiscus.sdk.event;

import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.util.List;

/**
 * Created on : February 08, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusDeleteCommentsEvent {
    private QiscusRoomMember actor;
    private boolean hardDelete;
    private List<DeletedComment> deletedComments;

    public QiscusRoomMember getActor() {
        return actor;
    }

    public void setActor(QiscusRoomMember actor) {
        this.actor = actor;
    }

    public boolean isHardDelete() {
        return hardDelete;
    }

    public void setHardDelete(boolean hardDelete) {
        this.hardDelete = hardDelete;
    }

    public List<DeletedComment> getDeletedComments() {
        return deletedComments;
    }

    public void setDeletedComments(List<DeletedComment> deletedComments) {
        this.deletedComments = deletedComments;
    }

    @Override
    public String toString() {
        return "QiscusDeleteCommentsEvent{" +
                "actor=" + actor +
                ", hardDelete=" + hardDelete +
                ", deletedComments=" + deletedComments +
                '}';
    }

    public static class DeletedComment {
        private long roomId;
        private String commentUniqueId;

        public DeletedComment(long roomId, String commentUniqueId) {
            this.roomId = roomId;
            this.commentUniqueId = commentUniqueId;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getCommentUniqueId() {
            return commentUniqueId;
        }

        @Override
        public String toString() {
            return "DeletedComment{" +
                    "roomId=" + roomId +
                    ", commentUniqueId='" + commentUniqueId + '\'' +
                    '}';
        }
    }
}
