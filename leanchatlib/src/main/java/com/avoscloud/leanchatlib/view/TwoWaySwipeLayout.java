package com.avoscloud.leanchatlib.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by wli on 15/11/26.
 * TODO 还需要优化，现在只实现功能，没有 UI 及动画
 */
public class TwoWaySwipeLayout extends SwipeRefreshLayout {

  private final int touchSlop;
  private RecyclerView recyclerView;
  private OnLoadmoreListener onLoadmoreListener;

  private float firstTouchY;
  private float lastTouchY;

  private boolean isLoading = false;

  public TwoWaySwipeLayout(Context context) {
    this(context, null);
  }

  public TwoWaySwipeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  public void setChildView(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    final int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        firstTouchY = event.getRawY();
        break;

      case MotionEvent.ACTION_UP:
        lastTouchY = event.getRawY();
        if (canLoadMore()) {
          loadData();
        }
        break;
      default:
        break;
    }

    return super.dispatchTouchEvent(event);
  }

  private boolean canLoadMore() {
    return isBottom() && !isLoading && isPullingUp();
  }

  private boolean isBottom() {
    if (null != recyclerView) {
      int itemsCount = recyclerView.getAdapter().getItemCount();
      if (itemsCount > 0) {
        int lastVisiblePisition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        if (lastVisiblePisition == itemsCount - 1) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isPullingUp() {
    return (firstTouchY - lastTouchY) >= touchSlop;
  }

  private void loadData() {
    if (onLoadmoreListener != null) {
      setLoading(true);
    }
  }

  public void setLoading(boolean loading) {
    if (recyclerView == null) return;
    isLoading = loading;
    if (loading) {
      if (isRefreshing()) {
        setRefreshing(false);
      }
      ((LinearLayoutManager)recyclerView.getLayoutManager()).scrollToPositionWithOffset(recyclerView.getAdapter().getItemCount() - 1, 0);
      onLoadmoreListener.onLoad();
    } else {
      firstTouchY = 0;
      lastTouchY = 0;
    }
  }

  public void setOnLoadListener(OnLoadmoreListener loadListener) {
    onLoadmoreListener = loadListener;
  }

  public interface OnLoadmoreListener {
    public void onLoad();
  }
}