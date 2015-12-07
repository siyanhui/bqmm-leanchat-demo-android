package com.avoscloud.leanchatlib_demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationEventHandler;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 登陆页面
 */
public class LoginActivity extends AVBaseActivity {
  @Bind(R.id.activity_login_et_username)
  protected EditText nameView;

  @Bind(R.id.activity_login_btn_login)
  protected Button loginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
  }

  @OnClick(R.id.activity_login_btn_login)
  public void onLoginClick(View view) {
    String clientId = nameView.getText().toString();
    initChatManager(clientId);
    ChatManager.getInstance().openClient(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
        } else {
          showToast(e.toString());
        }
      }
    });
  }

  private void initChatManager(String userId) {
    final ChatManager chatManager = ChatManager.getInstance();
    chatManager.init(this);
    if (!TextUtils.isEmpty(userId)) {
      chatManager.setupManagerWithUserId(userId);
    }
    chatManager.setConversationEventHandler(ConversationEventHandler.getInstance());
  }
}
