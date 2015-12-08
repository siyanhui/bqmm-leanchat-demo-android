package com.avoscloud.leanchatlib.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;

import java.util.Arrays;

/**
 * Created by wli on 15/12/7.
 */
public class RefreshableRecyclerView extends RecyclerView {
  private final int DEFAULT_PAGE_NUM = 5;
  public static int STATUS_NORMAL = 0;
  public static int STATUS_LAOD_MORE = 2;

  public final double VISIBLE_SCALE = 0.75;

  private int pageNum = DEFAULT_PAGE_NUM;
  private int loadStatus = STATUS_NORMAL;
  public boolean enableLoadMore = true;

  private SwipeRefreshLayout swipeRefreshLayout;
  private HeaderListAdapter headerListAdapter;
  private LoadMoreFooterView loadMoreFooterView;
  private OnLoadDataListener onLoadDataListener;

  public RefreshableRecyclerView(Context context) {
    super(context);
    initView();
  }

  public RefreshableRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public RefreshableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView();
  }

  public void setRelationSwipeLayout(SwipeRefreshLayout relationSwipeLayout) {
    swipeRefreshLayout = relationSwipeLayout;
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        startRefresh();
      }
    });
  }

  public void setRelationAdapter(HeaderListAdapter adapter) {
    headerListAdapter = adapter;
    headerListAdapter.setFooterView(loadMoreFooterView);
  }

  public void setPageNum(int pageNum) {
    this.pageNum = pageNum;
  }

  public void refreshData() {
    startRefresh();
  }

  public void setOnLoadDataListener(OnLoadDataListener loadDataListener) {
    onLoadDataListener = loadDataListener;
  }

  private void initView() {
    loadMoreFooterView = new LoadMoreFooterView(getContext());
    loadMoreFooterView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (enableLoadMore && STATUS_LAOD_MORE != getLoadStatus()) {
          startLoad();
        }
      }
    });
    addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
          if (enableLoadMore && STATUS_LAOD_MORE != getLoadStatus()) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
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

  private void startRefresh() {
    if (null != onLoadDataListener) {
      onLoadDataListener.onLoad(0, pageNum, true);
    }
  }

  private void startLoad() {
    if (STATUS_LAOD_MORE != getLoadStatus()) {
      if (null != onLoadDataListener) {
        setLoadStatus(STATUS_LAOD_MORE);
        onLoadDataListener.onLoad(headerListAdapter.getDataList().size(), pageNum, false);
      } else {
        setLoadStatus(STATUS_NORMAL);
      }
    }
  }

  public void setEnableLoadMore(boolean enable) {
    enableLoadMore = enable;
  }

  private void setLoadStatus(int status) {
    loadStatus = status;
    loadMoreFooterView.onLoadStatusChanged(status);
  }

  public void setLoadComplete() {
    setLoadStatus(STATUS_NORMAL);
    if (null != swipeRefreshLayout) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  public int getLoadStatus() {
    return loadStatus;
  }

  public void setLoadComplete(Object[] datas, boolean isRefresh) {
    setLoadStatus(STATUS_NORMAL);
    if (isRefresh) {
      headerListAdapter.setDataList(Arrays.asList(datas));
      headerListAdapter.notifyDataSetChanged();
      swipeRefreshLayout.setRefreshing(false);
    } else{
      headerListAdapter.addDataList(Arrays.asList(datas));
      headerListAdapter.notifyDataSetChanged();
    }
  }

  public interface OnLoadDataListener {
    public void onLoad(int skip, int limit, boolean isRefresh);
  }
}
