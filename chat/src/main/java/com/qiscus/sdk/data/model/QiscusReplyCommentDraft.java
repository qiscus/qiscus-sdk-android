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

package com.qiscus.sdk.data.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : September 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusReplyCommentDraft extends QiscusCommentDraft {
    private String repliedPayload;

    public QiscusReplyCommentDraft(String message, QiscusComment repliedComment) {
        super(message);
        JSONObject json = new JSONObject();
        try {
            json.put("text", message)
                    .put("replied_comment_id", repliedComment.getId())
                    .put("replied_comment_message", repliedComment.getMessage())
                    .put("replied_comment_sender_username", repliedComment.getSender())
                    .put("replied_comment_sender_email", repliedComment.getSenderEmail())
                    .put("replied_comment_type", repliedComment.getRawType())
                    .put("replied_comment_payload", repliedComment.getExtraPayload());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        repliedPayload = json.toString();
    }

    public QiscusReplyCommentDraft(String message, String repliedPayload) {
        super(message);
        this.repliedPayload = repliedPayload;
    }

    public String getRepliedPayload() {
        return repliedPayload;
    }

    public QiscusComment getRepliedComment() {
        QiscusComment replyTo = null;
        try {
            JSONObject payload = new JSONObject(repliedPayload);
            replyTo = new QiscusComment();
            replyTo.setId(payload.getString("replied_comment_id"));
            replyTo.setUniqueId(replyTo.getId() + "");
            replyTo.setMessage(payload.getString("replied_comment_message"));
            replyTo.setSender(payload.getString("replied_comment_sender_username"));
            replyTo.setSenderEmail(payload.getString("replied_comment_sender_email"));
            replyTo.setRawType(payload.optString("replied_comment_type"));
            replyTo.setExtraPayload(payload.optString("replied_comment_payload"));
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
