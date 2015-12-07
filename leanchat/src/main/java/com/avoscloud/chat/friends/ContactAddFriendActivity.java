package com.avoscloud.chat.friends;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.OnClick;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avoscloud.chat.R;
import com.avoscloud.chat.App;
import com.avoscloud.chat.event.SearchUserItemClickEvent;
import com.avoscloud.chat.viewholder.SearchUserItemHolder;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.chat.util.UserCacheUtils;
import com.avoscloud.leanchatlib.view.CustomRecyclerView;
import com.avoscloud.leanchatlib.view.LoadMoreFooterView;

import java.util.ArrayList;
import java.util.List;

/**
 * 查找好友页面
 */
public class ContactAddFriendActivity extends AVBaseActivity {

  @Bind(R.id.search_user_rv_layout)
  protected CustomRecyclerView recyclerView;

  @Bind(R.id.searchNameEdit)
  EditText searchNameEdit;

  private HeaderListAdapter<LeanchatUser> adapter;
  private String searchName = "";

  Handler handler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_add_friend_activity);
    init();
    loadMoreFriend(0, true);
  }

  private void init() {
    setTitle(App.ctx.getString(R.string.contact_findFriends));
    adapter = new HeaderListAdapter<>(SearchUserItemHolder.class);

    LoadMoreFooterView footerView = new LoadMoreFooterView(this);
    adapter.setFooterView(footerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setOnLoadMoreStatusChangedListener(footerView);
    recyclerView.setAdapter(adapter);
    recyclerView.setOnLoadMoreListener(new CustomRecyclerView.OnLoadMoreListener() {
      @Override
      public void onLoadMore() {
        loadMoreFriend(adapter.getDataList().size(), false);
      }
    });
  }

  private void loadMoreFriend(final int skip, final boolean isRefresh) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          AVQuery<LeanchatUser> q = LeanchatUser.getQuery(LeanchatUser.class);
          q.whereContains(LeanchatUser.USERNAME, searchName);
          q.limit(Constants.PAGE_SIZE);
          q.skip(skip);
          LeanchatUser user = LeanchatUser.getCurrentUser();
          List<String> friendIds = new ArrayList<String>(FriendsManager.getFriendIds());
          friendIds.add(user.getObjectId());
          q.whereNotContainedIn(Constants.OBJECT_ID, friendIds);
          q.orderByDescending(Constants.UPDATED_AT);
          q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
          final List<LeanchatUser> users = q.find();
          UserCacheUtils.cacheUsers(users);

          handler.post(new Runnable() {
            @Override
            public void run() {
              recyclerView.setLoadComplete();
              if (isRefresh) {
                adapter.setDataList(users);
              } else {
                adapter.addDataList(users);
              }
              adapter.notifyDataSetChanged();
            }
          });

        } catch (AVException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  @OnClick(R.id.searchBtn)
  public void search(View view) {
    searchName = searchNameEdit.getText().toString();
    loadMoreFriend(0, true);
  }

  public void onEvent(SearchUserItemClickEvent itemClickEvent) {
    Intent intent = new Intent(this, ContactPersonInfoActivity.class);
    intent.putExtra(Constants.LEANCHAT_USER_ID, itemClickEvent.memberId);
    startActivity(intent);
  }
}
