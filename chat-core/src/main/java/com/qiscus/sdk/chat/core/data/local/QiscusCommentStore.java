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

import com.qiscus.sdk.chat.core.data.model.QMessage;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created on : November 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface QiscusCommentStore {
    void add(QMessage qMessage);

    boolean isContains(QMessage qMessage);

    void update(QMessage qMessage);

    void addOrUpdate(QMessage qMessage);

    void delete(QMessage qMessage);

    boolean deleteCommentsByRoomId(long roomId);

    boolean deleteCommentsByRoomId(long roomId, long timestampOffset);

    void updateLastDeliveredComment(long roomId, long commentId);

    void updateLastReadComment(long roomId, long commentId);

    QMessage getComment(String uniqueId);

    QMessage getCommentByBeforeId(long beforeId);

    List<QMessage> getComments(long roomId);

    List<QMessage> getComments(long roomId, int limit);

    List<QMessage> getComments(long roomId, long timestampOffset);

    io.reactivex.Observable<List<QMessage>> getObservableComments(long roomId);

    io.reactivex.Observable<List<QMessage>> getObservableComments(long roomId, int limit);

    List<QMessage> getOlderCommentsThan(QMessage qMessage, long roomId, int limit);

    io.reactivex.Observable<List<QMessage>> getObservableOlderCommentsThan(QMessage qMessage, long roomId, int limit);

    List<QMessage> getCommentsAfter(QMessage qMessage, long roomId);

    io.reactivex.Observable<List<QMessage>> getObservableCommentsAfter(QMessage qMessage, long roomId);

    QMessage getLatestComment();

    QMessage getLatestComment(long roomId);

    QMessage getLatestDeliveredComment(long roomId);

    QMessage getLatestReadComment(long roomId);

    List<QMessage> getPendingComments();

    Observable<List<QMessage>> getObservablePendingComments();

    List<QMessage> searchComments(String query, long roomId, int limit, int offset);

    List<QMessage> searchComments(String query, int limit, int offset);
}
