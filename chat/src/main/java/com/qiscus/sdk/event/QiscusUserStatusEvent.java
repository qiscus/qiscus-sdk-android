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

import java.util.Date;

/**
 * Created on : October 26, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusUserStatusEvent {
    private String user;
    private boolean online;
    private Date lastActive;

    public QiscusUserStatusEvent(String user, boolean online, Date lastActive) {
        this.user = user;
        this.online = online;
        this.lastActive = lastActive;
    }

    public String getUser() {
        return user;
    }

    public boolean isOnline() {
        return online;
    }

    public Date getLastActive() {
        return lastActive;
    }
}
