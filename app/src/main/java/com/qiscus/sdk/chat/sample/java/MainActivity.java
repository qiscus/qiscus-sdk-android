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

package com.qiscus.sdk.chat.sample.java;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.qiscus.sdk.chat.core.Qiscus;
import com.qiscus.sdk.chat.core.QiscusUseCaseFactory;
import com.qiscus.sdk.chat.domain.interactor.account.AuthenticateWithKey;
import com.qiscus.sdk.chat.domain.interactor.room.GetRoomWithUserId;
import com.qiscus.sdk.chat.sample.R;

public class MainActivity extends AppCompatActivity {
    private QiscusUseCaseFactory useCaseFactory = Qiscus.Companion.getInstance().getUseCaseFactory();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.loginButton).setOnClickListener(view -> authenticate());

        findViewById(R.id.startChatButton).setOnClickListener(view -> openChatWith("rya.meyvriska244@gmail.com"));
    }

    private void authenticate() {
        AuthenticateWithKey authenticate = useCaseFactory.authenticateWithKey();

        authenticate.execute(new AuthenticateWithKey.Params("zetra255@gmail.com", "12345678", "Zetra"),
                account -> Log.d("ZETRA", "Login with " + account), Throwable::printStackTrace);
    }

    private void openChatWith(String userId) {
        GetRoomWithUserId getRoom = useCaseFactory.getRoomWithUserId();

        getRoom.execute(new GetRoomWithUserId.Params(userId), room -> Log.d("ZETRA", "Room: " + room));
    }
}
