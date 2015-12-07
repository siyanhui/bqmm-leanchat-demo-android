package com.avoscloud.chat.friends;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.avoscloud.chat.R;
import com.avoscloud.chat.activity.ChatRoomActivity;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.leanchatlib.utils.PhotoUtils;
import com.avoscloud.chat.util.UserCacheUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * 用户详情页，从对话详情页面和发现页面跳转过来
 */
public class ContactPersonInfoActivity extends AVBaseActivity implements OnClickListener {
  public static final String USER_ID = "userId";
  TextView usernameView, genderView;
  ImageView avatarView, avatarArrowView;
  LinearLayout allLayout;
  Button chatBtn, addFriendBtn;
  RelativeLayout avatarLayout, genderLayout;

  String userId = "";
  LeanchatUser user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_person_info_activity);
    initData();

    findView();
    initView();
  }

  private void initData() {
    userId = getIntent().getStringExtra(Constants.LEANCHAT_USER_ID);
    user = UserCacheUtils.getCachedUser(userId);
  }

  private void findView() {
    allLayout = (LinearLayout) findViewById(R.id.all_layout);
    avatarView = (ImageView) findViewById(R.id.avatar_view);
    avatarArrowView = (ImageView) findViewById(R.id.avatar_arrow);
    usernameView = (TextView) findViewById(R.id.username_view);
    avatarLayout = (RelativeLayout) findViewById(R.id.head_layout);
    genderLayout = (RelativeLayout) findViewById(R.id.sex_layout);

    genderView = (TextView) findViewById(R.id.sexView);
    chatBtn = (Button) findViewById(R.id.chatBtn);
    addFriendBtn = (Button) findViewById(R.id.addFriendBtn);
  }

  private void initView() {
    LeanchatUser curUser = LeanchatUser.getCurrentUser();
    if (curUser.equals(user)) {
      setTitle(R.string.contact_personalInfo);
      avatarLayout.setOnClickListener(this);
      genderLayout.setOnClickListener(this);
      avatarArrowView.setVisibility(View.VISIBLE);
      chatBtn.setVisibility(View.GONE);
      addFriendBtn.setVisibility(View.GONE);
    } else {
      setTitle(R.string.contact_detailInfo);
      avatarArrowView.setVisibility(View.INVISIBLE);
      List<String> cacheFriends = FriendsManager.getFriendIds();
      boolean isFriend = cacheFriends.contains(user.getObjectId());
      if (isFriend) {
        chatBtn.setVisibility(View.VISIBLE);
        chatBtn.setOnClickListener(this);
      } else {
        chatBtn.setVisibility(View.GONE);
        addFriendBtn.setVisibility(View.VISIBLE);
        addFriendBtn.setOnClickListener(this);
      }
    }
    updateView(user);
  }

  private void updateView(LeanchatUser user) {
    ImageLoader.getInstance().displayImage(user.getAvatarUrl(), avatarView, PhotoUtils.avatarImageOptions);
    usernameView.setText(user.getUsername());
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.chatBtn:// 发起聊天
        Intent intent = new Intent(ContactPersonInfoActivity.this, ChatRoomActivity.class);
        intent.putExtra(Constants.MEMBER_ID, userId);
        startActivity(intent);
        finish();
        break;
      case R.id.addFriendBtn:// 添加好友
        AddRequestManager.getInstance().createAddRequestInBackground(this, user);
        break;
    }
  }
}
