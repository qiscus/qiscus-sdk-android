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

import com.qiscus.sdk.data.model.QiscusComment;

import java.util.List;

import rx.Observable;

/**
 * Created on : November 07, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface QiscusCommentStore {
    void add(QiscusComment qiscusComment);

    boolean isContains(QiscusComment qiscusComment);

    void update(QiscusComment qiscusComment);

    void addOrUpdate(QiscusComment qiscusComment);

    void delete(QiscusComment qiscusComment);

    boolean deleteCommentsByRoomId(long roomId);

    boolean deleteCommentsByRoomId(long roomId, long timestampOffset);

    void updateLastDeliveredComment(long roomId, long commentId);

    void updateLastReadComment(long roomId, long commentId);

    QiscusComment getComment(String uniqueId);

    QiscusComment getComment(long id);

    QiscusComment getCommentByBeforeId(long beforeId);

    List<QiscusComment> getComments(long roomId);

    List<QiscusComment> getComments(long roomId, int limit);

    List<QiscusComment> getComments(long roomId, long timestampOffset);

    Observable<List<QiscusComment>> getObservableComments(long roomId);

    Observable<List<QiscusComment>> getObservableComments(long roomId, int limit);

    List<QiscusComment> getOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit);

    Observable<List<QiscusComment>> getObservableOlderCommentsThan(QiscusComment qiscusComment, long roomId, int limit);

    List<QiscusComment> getCommentsAfter(QiscusComment qiscusComment, long roomId);

    Observable<List<QiscusComment>> getObservableCommentsAfter(QiscusComment qiscusComment, long roomId);

    QiscusComment getLatestComment();

    QiscusComment getLatestComment(long roomId);

    QiscusComment getLatestDeliveredComment(long roomId);

    QiscusComment getLatestReadComment(long roomId);

    List<QiscusComment> getPendingComments();

    Observable<List<QiscusComment>> getObservablePendingComments();

    List<QiscusComment> searchComments(String query, long roomId, int limit, int offset);

    List<QiscusComment> searchComments(String query, int limit, int offset);

}
