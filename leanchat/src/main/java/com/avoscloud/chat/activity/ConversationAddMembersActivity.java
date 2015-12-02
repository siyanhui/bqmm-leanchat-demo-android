package com.avoscloud.chat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.MemeberAddAdapter;
import com.avoscloud.chat.friends.FriendsManager;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.UserCacheUtils;
import com.avoscloud.leanchatlib.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
/**
 * 群聊对话拉人页面
 * Created by lzw on 14-10-11.
 * TODO: ConversationChangeEvent
 */
public class ConversationAddMembersActivity extends AVBaseActivity {

  @InjectView(R.id.member_add_rv_list)
  protected RecyclerView recyclerView;

  private LinearLayoutManager layoutManager;
  private MemeberAddAdapter adapter;
  private AVIMConversation conversation;

  public static final int OK = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.conversation_add_members_layout);
    ButterKnife.inject(this);
    String conversationId = getIntent().getStringExtra(Constants.CONVERSATION_ID);
    conversation = AVIMClient.getInstance(ChatManager.getInstance().getSelfId()).getConversation(conversationId);

    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new MemeberAddAdapter();
    recyclerView.setAdapter(adapter);

    initActionBar();
    setListData();
  }

  private void setListData() {
    FriendsManager.fetchFriends(false, new FindCallback<LeanchatUser>() {
      @Override
      public void done(List<LeanchatUser> list, AVException e) {
        if (filterException(e)) {
          final List<String> userIds = new ArrayList<String>();
          for (LeanchatUser user : list) {
            userIds.add(user.getObjectId());
          }
          userIds.removeAll(conversation.getMembers());
          UserCacheUtils.fetchUsers(userIds, new UserCacheUtils.CacheUserCallback() {
            @Override
            public void done(List<LeanchatUser> userList, Exception e) {
              adapter.setDataList(userList);
              adapter.notifyDataSetChanged();
            }
          });
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, OK, 0, R.string.common_sure);
    alwaysShowMenuItem(add);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == OK) {
      addMembers();
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void addMembers() {
    final List<String> checkedUsers = adapter.getCheckedIds();
    final ProgressDialog dialog = showSpinnerDialog();
    if (checkedUsers.size() == 0) {
      finish();
    } else {
      if (ConversationHelper.typeOfConversation(conversation) == ConversationType.Single) {
        List<String> members = new ArrayList<String>();
        members.addAll(checkedUsers);
        members.addAll(conversation.getMembers());
        ConversationManager.getInstance().createGroupConversation(members, new AVIMConversationCreatedCallback() {
          @Override
          public void done(final AVIMConversation conversation, AVIMException e) {
            if (filterException(e)) {
              Intent intent = new Intent(ConversationAddMembersActivity.this, ChatRoomActivity.class);
              intent.putExtra(Constants.CONVERSATION_ID, conversation.getConversationId());
              startActivity(intent);
              finish();
            }
          }
        });
      } else {
        conversation.addMembers(checkedUsers, new AVIMConversationCallback() {
          @Override
          public void done(AVIMException e) {
            dialog.dismiss();
            if (filterException(e)) {
              Utils.toast(R.string.conversation_inviteSucceed);
              setResult(RESULT_OK);
              finish();
            }
          }
        });
      }
    }
  }
}
