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

package com.qiscus.sdk.presenter;

import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on : October 31, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusUserStatusPresenter extends QiscusPresenter<QiscusUserStatusPresenter.View> {
    private Set<String> users;

    public QiscusUserStatusPresenter(View view) {
        super(view);
        users = new HashSet<>();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void listenUser(String user) {
        if (!users.contains(user)) {
            users.add(user);
            QiscusPusherApi.getInstance().subscribeUserOnlinePresence(user);
        }
    }

    @Subscribe
    public void onUserStatusChanged(QiscusUserStatusEvent event) {
        if (users.contains(event.getUser())) {
            view.onUserStatusChanged(event.getUser(), event.isOnline(), event.getLastActive());
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        for (String user : users) {
            QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence(user);
        }
        EventBus.getDefault().unregister(this);
    }

    public interface View extends QiscusPresenter.View {
        void onUserStatusChanged(String user, boolean online, Date lastActive);
    }
}
