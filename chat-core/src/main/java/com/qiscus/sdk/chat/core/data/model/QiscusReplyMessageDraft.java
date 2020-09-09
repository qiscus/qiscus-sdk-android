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

package com.qiscus.sdk.chat.core.data.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : September 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusReplyMessageDraft extends QMessageDraft {
    private String repliedPayload;

    public QiscusReplyMessageDraft(String message, QMessage repliedMessage) {
        super(message);
        JSONObject json = new JSONObject();
        try {
            json.put("text", message)
                    .put("replied_comment_id", repliedMessage.getId())
                    .put("replied_comment_message", repliedMessage.getText())
                    .put("replied_comment_sender_username", repliedMessage.getSender())
                    .put("replied_comment_sender_email", repliedMessage.getSender().getId())
                    .put("replied_comment_type", repliedMessage.getRawType())
                    .put("replied_comment_payload", repliedMessage.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        repliedPayload = json.toString();
    }

    public QiscusReplyMessageDraft(String message, String repliedPayload) {
        super(message);
        this.repliedPayload = repliedPayload;
    }

    public String getRepliedPayload() {
        return repliedPayload;
    }

    public QMessage getRepliedComment() {
        QMessage replyTo = null;
        try {
            JSONObject payload = new JSONObject(repliedPayload);
            replyTo = new QMessage();
            replyTo.setId(payload.getLong("replied_comment_id"));
            replyTo.setUniqueId(replyTo.getId() + "");
            replyTo.setText(payload.getString("replied_comment_message"));

            QUser qUser = new QUser();
            qUser.setName(payload.getString("replied_comment_sender_username"));
            qUser.setId(payload.getString("replied_comment_sender_email"));

            replyTo.setSender(qUser);
            replyTo.getSender().setId(payload.getString("replied_comment_sender_email"));
            replyTo.setRawType(payload.optString("replied_comment_type"));
            replyTo.setPayload(payload.optString("replied_comment_payload"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return replyTo;
    }

    @Override
    public String toString() {
        return "QiscusReplyCommentDraft{" +
                "repliedPayload='" + repliedPayload + '\'' +
                '}';
    }
}
