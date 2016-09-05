package com.qiscus.sdk.event;

import com.qiscus.sdk.data.model.QiscusComment;

public class CommentReceivedEvent {
    private QiscusComment qiscusComment;

    public CommentReceivedEvent(QiscusComment qiscusComment) {
        this.qiscusComment = qiscusComment;
    }

    public QiscusComment getQiscusComment() {
        return qiscusComment;
    }
}
