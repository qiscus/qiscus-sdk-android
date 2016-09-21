package com.qiscus.sdk.event;


import com.qiscus.sdk.data.model.QiscusComment;

public class QiscusCommentReceivedEvent {
    private QiscusComment qiscusComment;

    public QiscusCommentReceivedEvent(QiscusComment qiscusComment) {
        this.qiscusComment = qiscusComment;
    }

    public QiscusComment getQiscusComment() {
        return qiscusComment;
    }
}
