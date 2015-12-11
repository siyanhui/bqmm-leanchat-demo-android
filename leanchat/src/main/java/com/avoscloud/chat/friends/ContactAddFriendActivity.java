package com.avoscloud.chat.friends;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.OnClick;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.App;
import com.avoscloud.chat.viewholder.SearchUserItemHolder;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.chat.util.UserCacheUtils;
import com.avoscloud.leanchatlib.view.RefreshableRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 查找好友页面
 */
public class ContactAddFriendActivity extends AVBaseActivity {

  @Bind(R.id.search_user_rv_layout)
  protected RefreshableRecyclerView recyclerView;

  @Bind(R.id.searchNameEdit)
  EditText searchNameEdit;

  private HeaderListAdapter<LeanchatUser> adapter;
  private String searchName = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_add_friend_activity);
    init();
    recyclerView.refreshData();
  }

  private void init() {
    setTitle(App.ctx.getString(R.string.contact_findFriends));
    adapter = new HeaderListAdapter<>(SearchUserItemHolder.class);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setOnLoadDataListener(new RefreshableRecyclerView.OnLoadDataListener() {
      @Override
      public void onLoad(int skip, int limit, boolean isRefresh) {
        loadMoreFriend(skip, limit, isRefresh);
      }
    });
    recyclerView.setAdapter(adapter);
  }

  private void loadMoreFriend(int skip, final int limit, final boolean isRefresh) {
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
    q.findInBackground(new FindCallback<LeanchatUser>() {
      @Override
      public void done(List<LeanchatUser> list, AVException e) {
        UserCacheUtils.cacheUsers(list);
        recyclerView.setLoadComplete(list.toArray(), false);
      }
    });
  }

  @OnClick(R.id.searchBtn)
  public void search(View view) {
    searchName = searchNameEdit.getText().toString();
    recyclerView.refreshData();
  }
}
