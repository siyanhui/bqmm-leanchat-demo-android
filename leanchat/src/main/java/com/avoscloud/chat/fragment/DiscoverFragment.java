package com.avoscloud.chat.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVQuery;
import com.avoscloud.chat.R;
import com.avoscloud.chat.App;
import com.avoscloud.chat.friends.ContactPersonInfoActivity;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.leanchatlib.utils.UserCacheUtils;
import com.avoscloud.leanchatlib.view.CustomRecyclerView;
import com.avoscloud.leanchatlib.view.LoadMoreFooterView;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.leanchatlib.utils.Logger;
import com.avoscloud.chat.viewholder.DiscoverItemHolder;
import com.avoscloud.leanchatlib.event.DiscoverItemClickEvent;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class DiscoverFragment extends BaseFragment {

  private final SortDialogListener distanceListener = new SortDialogListener(Constants.ORDER_DISTANCE);
  private final SortDialogListener updatedAtListener = new SortDialogListener(Constants.ORDER_UPDATED_AT);

  @InjectView(R.id.fragment_near_srl_pullrefresh)
  protected SwipeRefreshLayout refreshLayout;

  @InjectView(R.id.fragment_near_srl_view)
  protected CustomRecyclerView recyclerView;

  protected LinearLayoutManager layoutManager;

  HeaderListAdapter<LeanchatUser> discoverAdapter;
  int orderType;
  PreferenceMap preferenceMap;

  Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.discover_fragment, container, false);
    ButterKnife.inject(this, view);
    EventBus.getDefault().register(this);

    LoadMoreFooterView footerView = new LoadMoreFooterView(getActivity());
    layoutManager = new LinearLayoutManager(getActivity());
    discoverAdapter = new HeaderListAdapter<>(DiscoverItemHolder.class);
    discoverAdapter.setFooterView(footerView);
    recyclerView.setOnLoadMoreStatusChangedListener(footerView);
    recyclerView.setOnLoadMoreListener(new CustomRecyclerView.OnLoadMoreListener() {
      @Override
      public void onLoadMore() {
        loadMoreDiscoverData();
      }
    });
    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        refreshDiscoverList();
      }
    });
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(discoverAdapter);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    preferenceMap = PreferenceMap.getCurUserPrefDao(getActivity());
    orderType = preferenceMap.getNearbyOrder();
    headerLayout.showTitle(R.string.discover_title);
    headerLayout.showRightImageButton(R.drawable.nearby_order, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.discover_fragment_sort).setPositiveButton(R.string.discover_fragment_loginTime,
          updatedAtListener).setNegativeButton(R.string.discover_fragment_distance, distanceListener).show();
      }
    });
    refreshDiscoverList();
  }

  private void loadMoreDiscoverData() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final List<LeanchatUser> userList = findNearbyPeople(Constants.ORDER_DISTANCE, discoverAdapter.getItemCount(), 20);
          handler.post(new Runnable() {
            @Override
            public void run() {
              recyclerView.setLoadComplete();
              discoverAdapter.addDataList(userList);
              discoverAdapter.notifyDataSetChanged();
            }
          });

        } catch (AVException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void refreshDiscoverList() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final List<LeanchatUser> userList = findNearbyPeople(Constants.ORDER_DISTANCE, 0, 20);
          handler.post(new Runnable() {
            @Override
            public void run() {
              refreshLayout.setRefreshing(false);
              discoverAdapter.setDataList(userList);
              discoverAdapter.notifyDataSetChanged();
            }
          });

        } catch (AVException e) {
          e.printStackTrace();
        }
      }
    }).start();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    preferenceMap.setNearbyOrder(orderType);
  }

  public List<LeanchatUser> findNearbyPeople(int orderType, int skip, int limit) throws AVException {
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint geoPoint = preferenceMap.getLocation();
    if (geoPoint == null) {
      Logger.i("geo point is null");
      return new ArrayList<>();
    }
    AVQuery<LeanchatUser> q = LeanchatUser.getQuery(LeanchatUser.class);
    LeanchatUser user = LeanchatUser.getCurrentUser();
    q.whereNotEqualTo(Constants.OBJECT_ID, user.getObjectId());
    if (orderType == Constants.ORDER_DISTANCE) {
      q.whereNear(LeanchatUser.LOCATION, geoPoint);
    } else {
      q.orderByDescending(Constants.UPDATED_AT);
    }
    q.skip(skip);
    q.limit(limit);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<LeanchatUser> users = q.find();
    UserCacheUtils.cacheUsers(users);
    return users;
  }

  public void onEvent(DiscoverItemClickEvent clickEvent) {
    Intent intent = new Intent(getActivity(), ContactPersonInfoActivity.class);
    intent.putExtra(Constants.LEANCHAT_USER_ID, clickEvent.userId);
    startActivity(intent);
  }

  public class SortDialogListener implements DialogInterface.OnClickListener {
    int orderType;

    public SortDialogListener(int orderType) {
      this.orderType = orderType;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      DiscoverFragment.this.orderType = orderType;
      refreshDiscoverList();
    }
  }
}
