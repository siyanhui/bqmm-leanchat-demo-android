package com.avoscloud.leanchatlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wli on 15/12/3.
 * TODO 关于下拉刷新，此处还需要优化，这样还是太麻烦了
 */
public class CustomRecyclerView extends RecyclerView {

  public static int STATUS_NORMAL = 0;
  public static int STATUS_LAOD_MORE = 2;

  public final double VISIBLE_SCALE = 0.75;

  private int loadStatus = STATUS_NORMAL;
  public boolean enableLoadMore = true;

  public OnLoadMoreListener loadMoreListener;
  public OnLoadMoreStatusListener statusListener;

  public CustomRecyclerView(Context context) {
    super(context);
    initView();
  }

  public CustomRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView();
  }

  private void initView() {
    addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (enableLoadMore && STATUS_LAOD_MORE != getLoadStatus()) {
          LinearLayoutManager layoutManager = (LinearLayoutManager)getLayoutManager();
          int totalItemCount = layoutManager.getItemCount();
          int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
          if (lastVisibleItem == totalItemCount - 1) {
            View view = layoutManager.findViewByPosition(lastVisibleItem);
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            if (rect.height() / view.getHeight() > VISIBLE_SCALE) {
              startLoad();
            }
          }
        }
      }
    });
  }

  private void startLoad() {
    if (STATUS_LAOD_MORE != getLoadStatus()) {
      if (null != loadMoreListener) {
        setLoadStatus(STATUS_LAOD_MORE);
        loadMoreListener.onLoadMore();
      } else {
        setLoadStatus(STATUS_NORMAL);
      }
    }
  }

  public void setOnLoadMoreListener(OnLoadMoreListener listener) {
    loadMoreListener = listener;
  }

  public void setOnLoadMoreStatusChangedListener(OnLoadMoreStatusListener listener) {
    statusListener = listener;
  }

  public void setEnableLoadMore(boolean enable) {
    enableLoadMore = enable;
  }

  public int getLoadStatus() {
    return loadStatus;
  }

  private void setLoadStatus(int status) {
    loadStatus = status;
    if (null != statusListener) {
      statusListener.onLoadStatusChanged(status);
    }
  }

  public void setLoadComplete() {
    setLoadStatus(STATUS_NORMAL);
  }

  public interface OnLoadMoreListener {
    public void onLoadMore();
  }

  public interface OnLoadMoreStatusListener {
    public void onLoadStatusChanged(int staus);
  }
}
