package com.avoscloud.chat.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.App;
import com.avoscloud.chat.R;
import com.avoscloud.chat.activity.ChatRoomActivity;
import com.avoscloud.chat.activity.ContactAddFriendActivity;
import com.avoscloud.chat.activity.ContactNewFriendActivity;
import com.avoscloud.chat.activity.ConversationGroupListActivity;
import com.avoscloud.chat.adapter.ContactsAdapter;
import com.avoscloud.chat.event.ContactItemClickEvent;
import com.avoscloud.chat.event.ContactItemLongClickEvent;
import com.avoscloud.chat.event.ContactRefreshEvent;
import com.avoscloud.chat.event.InvitationEvent;
import com.avoscloud.chat.event.MemberLetterEvent;
import com.avoscloud.chat.service.AddRequestManager;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.leanchatlib.utils.UserCacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * 联系人列表
 *
 * TODO
 * 1、替换 Fragment 的 title
 * 2、优化 findFriends 代码，现在还是冗余
 */

public class ContactFragment extends BaseFragment {

  @InjectView(R.id.activity_square_members_srl_list)
  protected SwipeRefreshLayout refreshLayout;

  @InjectView(R.id.activity_square_members_rv_list)
  protected RecyclerView recyclerView;

  private View headerView;
  ImageView msgTipsView;

  private ContactsAdapter itemAdapter;
  LinearLayoutManager layoutManager;

  private Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    View view = inflater.inflate(R.layout.contact_fragment, container, false);
    headerView = inflater.inflate(R.layout.contact_fragment_header_layout, container, false);
    ButterKnife.inject(this, view);

    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    itemAdapter = new ContactsAdapter();
    itemAdapter.setHeaderView(headerView);
    recyclerView.setAdapter(itemAdapter);

    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        getMembers(false);
      }
    });
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initHeaderView();
    initHeader();
    refresh();
    EventBus.getDefault().register(this);
    getMembers(false);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateNewRequestBadge();
  }

  private void initHeaderView() {
    msgTipsView = (ImageView)headerView.findViewById(R.id.iv_msg_tips);
    View newView = headerView.findViewById(R.id.layout_new);
    newView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ctx, ContactNewFriendActivity.class);
        ctx.startActivity(intent);
      }
    });

    View groupView = headerView.findViewById(R.id.layout_group);
    groupView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ctx, ConversationGroupListActivity.class);
        ctx.startActivity(intent);
      }
    });
  }

  private void getMembers(final boolean isforce) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final List<LeanchatUser> users = findFriends(isforce);
          handler.post(new Runnable() {
            @Override
            public void run() {
              refreshLayout.setRefreshing(false);
              itemAdapter.setUserList(users);
              itemAdapter.notifyDataSetChanged();
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  private void initHeader() {
    headerLayout.showTitle(App.ctx.getString(R.string.contact));
    headerLayout.showRightImageButton(R.drawable.base_action_bar_add_bg_selector, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ctx, ContactAddFriendActivity.class);
        ctx.startActivity(intent);
      }
    });
  }

  private void updateNewRequestBadge() {
    msgTipsView.setVisibility(
      AddRequestManager.getInstance().hasUnreadRequests() ? View.VISIBLE : View.GONE);
  }

  private void refresh() {
    AddRequestManager.getInstance().countUnreadRequests(new CountCallback() {
      @Override
      public void done(int i, AVException e) {
        updateNewRequestBadge();
      }
    });
  }
  public void showDeleteDialog(final String memberId) {
    new AlertDialog.Builder(ctx).setMessage(R.string.contact_deleteContact)
        .setPositiveButton(R.string.common_sure, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final ProgressDialog dialog1 = showSpinnerDialog();
            LeanchatUser.getCurrentUser().removeFriend(memberId, new SaveCallback() {
              @Override
              public void done(AVException e) {
                dialog1.dismiss();
                if (filterException(e)) {
                  getMembers(true);
                }
              }
            });
          }
        }).setNegativeButton(R.string.chat_common_cancel, null).show();
  }

    public static List<LeanchatUser> findFriends(boolean isforce) throws Exception {
    final List<LeanchatUser> friends = new ArrayList<LeanchatUser>();
    final AVException[] es = new AVException[1];
    final CountDownLatch latch = new CountDownLatch(1);
      LeanchatUser.getCurrentUser().findFriendsWithCachePolicy(
        isforce ? AVQuery.CachePolicy.NETWORK_ELSE_CACHE : AVQuery.CachePolicy.CACHE_ELSE_NETWORK, new FindCallback<LeanchatUser>() {
        @Override
        public void done(List<LeanchatUser> avUsers, AVException e) {
          if (e != null) {
            es[0] = e;
          } else {
            friends.addAll(avUsers);
          }
          latch.countDown();
        }
      });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    } else {
      List<String> userIds = new ArrayList<String>();
      for (LeanchatUser user : friends) {
        userIds.add(user.getObjectId());
      }
      CacheService.setFriendIds(userIds);
      UserCacheUtils.fetchUsers(userIds);
      List<LeanchatUser> newFriends = new ArrayList<>();
      for (LeanchatUser user : friends) {
        newFriends.add(UserCacheUtils.getCachedUser(user.getObjectId()));
      }
      return newFriends;
    }
  }

  public void onEvent(ContactRefreshEvent event) {
    getMembers(true);
  }

  public void onEvent(InvitationEvent event) {
    AddRequestManager.getInstance().unreadRequestsIncrement();
    updateNewRequestBadge();
  }

  public void onEvent(ContactItemClickEvent event) {
    Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
    intent.putExtra(Constants.MEMBER_ID, event.memberId);
    startActivity(intent);
  }

  public void onEvent(ContactItemLongClickEvent event) {
    showDeleteDialog(event.memberId);
  }

  /**
   * 处理 LetterView 发送过来的 MemberLetterEvent
   * 会通过 MembersAdapter 获取应该要跳转到的位置，然后跳转
   */
  public void onEvent(MemberLetterEvent event) {
    Character targetChar = Character.toLowerCase(event.letter);
    if (itemAdapter.getIndexMap().containsKey(targetChar)) {
      int index = itemAdapter.getIndexMap().get(targetChar);
      if (index > 0 && index < itemAdapter.getItemCount()) {
        layoutManager.scrollToPositionWithOffset(index, 0);
      }
    }
  }
}
