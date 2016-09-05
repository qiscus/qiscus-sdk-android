package com.qiscus.library.chat.event;

import com.qiscus.library.chat.data.model.QiscusComment;

public class CommentReceivedEvent {
    private QiscusComment qiscusComment;

    public CommentReceivedEvent(QiscusComment qiscusComment) {
        this.qiscusComment = qiscusComment;
    }

    public QiscusComment getQiscusComment() {
        return qiscusComment;
    }
}
