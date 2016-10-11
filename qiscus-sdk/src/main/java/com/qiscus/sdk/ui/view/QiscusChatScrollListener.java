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

package com.qiscus.sdk.ui.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created on : June 01, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusChatScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager linearLayoutManager;
    private Listener listener;
    private boolean onTop;
    private boolean onBottom = true;
    private boolean onMiddle;

    public QiscusChatScrollListener(LinearLayoutManager linearLayoutManager, Listener listener) {
        this.linearLayoutManager = linearLayoutManager;
        this.listener = listener;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (linearLayoutManager.findFirstVisibleItemPosition() <= 0 && !onTop) {
            listener.onBottomOffListMessage();
            onBottom = true;
            onTop = false;
            onMiddle = false;
        } else if (linearLayoutManager.findLastVisibleItemPosition() >= linearLayoutManager.getItemCount() - 1 && !onBottom) {
            listener.onTopOffListMessage();
            onTop = true;
            onBottom = false;
            onMiddle = false;
        } else if (!onMiddle) {
            listener.onMiddleOffListMessage();
            onMiddle = true;
            onTop = false;
            onBottom = false;
        }
    }

    public interface Listener {
        void onTopOffListMessage();

        void onMiddleOffListMessage();

        void onBottomOffListMessage();
    }
}
