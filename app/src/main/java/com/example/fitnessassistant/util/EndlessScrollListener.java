package com.example.fitnessassistant.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private boolean isLoading;
    private boolean hasMorePages;
    private int pageNumber = 1;
    private final RefreshListener refreshListener;
    private boolean isRefreshing;

    public EndlessScrollListener(RefreshListener refreshListener) {
        this.isLoading = false;
        this.hasMorePages = true;
        this.refreshListener = refreshListener;
    }

    @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        LinearLayoutManager manager =
                (LinearLayoutManager) recyclerView.getLayoutManager();

        if(manager != null) {
            int visibleItemCount = manager.getChildCount();
            int totalItemCount = manager.getItemCount();
            int pastVisibleItems = manager.findFirstVisibleItemPosition();

            if (visibleItemCount + pastVisibleItems >= totalItemCount && !isLoading) {
                isLoading = true;
                if (hasMorePages && !isRefreshing) {
                    isRefreshing = true;
                    refreshListener.onRefresh(pageNumber);
                    notifyMorePages();
                }
            } else {
                isLoading = false;
            }
        }
    }

    public void noMorePages() {
        hasMorePages = false;
    }

    public void notifyMorePages() {
        hasMorePages = true;
        isRefreshing = false;
        pageNumber = pageNumber + 1;
    }

    public interface RefreshListener {
        void onRefresh(int pageNumber);
    }
}
