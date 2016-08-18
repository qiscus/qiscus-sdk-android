package com.qiscus.library.chat.ui.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import timber.log.Timber;

/**
 * Created on : June 01, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ChatScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager linearLayoutManager;
    private Listener listener;
    private boolean onTop;
    private boolean onBottom = true;
    private boolean onMiddle;

    public ChatScrollListener(LinearLayoutManager linearLayoutManager, Listener listener) {
        this.linearLayoutManager = linearLayoutManager;
        this.listener = listener;
        Timber.tag(ChatScrollListener.class.getSimpleName());
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
