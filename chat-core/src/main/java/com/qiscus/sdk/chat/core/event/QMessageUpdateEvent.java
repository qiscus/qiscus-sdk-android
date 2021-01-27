package com.qiscus.sdk.chat.core.event;

import com.qiscus.sdk.chat.core.data.model.QMessage;

public class QMessageUpdateEvent {
    private QMessage qMessage;

    public QMessageUpdateEvent(QMessage qMessage) {
        this.qMessage = qMessage;
    }

    public QMessage getQMessage() {
        return qMessage;
    }
}

