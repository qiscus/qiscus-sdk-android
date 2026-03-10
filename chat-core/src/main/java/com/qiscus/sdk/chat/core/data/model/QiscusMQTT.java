package com.qiscus.sdk.chat.core.data.model;

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
/**
 * Created on : August 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusMQTT {
    private String usernameMQTT;
    private String passwordMQTT;

    public QiscusMQTT(String usernameMQTT, String passwordMQTT) {
        this.usernameMQTT = usernameMQTT;
        this.passwordMQTT = passwordMQTT;
    }

    public String getUsernameMQTT() {
        return usernameMQTT;
    }

    public void setUsernameMQTT(String usernameMQTT) {
        this.usernameMQTT = usernameMQTT;
    }

    public String getPasswordMQTT() {
        return passwordMQTT;
    }

    public void setPasswordMQTT(String passwordMQTT) {
        this.passwordMQTT = passwordMQTT;
    }

    @Override
    public String toString() {
        return "QiscusMQTT{" +
                "usernameMQTT=" + usernameMQTT +
                ", passwordMQTT='" + passwordMQTT + '\'' +
                '}';
    }
}

