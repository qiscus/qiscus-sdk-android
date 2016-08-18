package com.qiscus.library.chat.ui.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class BaseRecyclerListener extends RecyclerView.OnScrollListener {
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 3;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private int currentPage = 0;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    public BaseRecyclerListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    public BaseRecyclerListener(GridLayoutManager gridLayoutManager) {
        this.gridLayoutManager = gridLayoutManager;
    }

    public BaseRecyclerListener(StaggeredGridLayoutManager staggeredGridLayoutManager) {
        this.staggeredGridLayoutManager = staggeredGridLayoutManager;
    }

    public BaseRecyclerListener(LinearLayoutManager linearLayoutManager, int visibleThreshold) {
        this.linearLayoutManager = linearLayoutManager;
        this.visibleThreshold = visibleThreshold;
    }

    public BaseRecyclerListener(GridLayoutManager gridLayoutManager, int visibleThreshold) {
        this.gridLayoutManager = gridLayoutManager;
        this.visibleThreshold = visibleThreshold;
    }

    public BaseRecyclerListener(StaggeredGridLayoutManager staggeredGridLayoutManager, int visibleThreshold) {
        this.staggeredGridLayoutManager = staggeredGridLayoutManager;
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        if (linearLayoutManager != null) {
            totalItemCount = linearLayoutManager.getItemCount();
            firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        } else if (gridLayoutManager != null) {
            totalItemCount = gridLayoutManager.getItemCount();
            firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
        } else if (staggeredGridLayoutManager != null) {
            totalItemCount = staggeredGridLayoutManager.getItemCount();
            int[] tmp = null;
            tmp = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(tmp);
            if (tmp != null && tmp.length > 0) {
                firstVisibleItem = tmp[0];
            }
        }

        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }
    }

    public void reset() {
        previousTotal = 0;
        loading = true;
        visibleThreshold = 3;
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
        currentPage = 0;
    }

    public abstract void onLoadMore(int currentPage);
}
