package com.qiscus.library.chat.event;

import com.qiscus.library.chat.data.model.Comment;

public class CommentReceivedEvent {
    private Comment comment;

    public CommentReceivedEvent(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }
}
