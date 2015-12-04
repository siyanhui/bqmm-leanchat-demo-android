package com.avoscloud.leanchatlib_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationEventHandler;

public class LoginActivity extends Activity implements View.OnClickListener {
  private EditText nameView;
  private Button loginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    nameView = (EditText) findViewById(R.id.login_tv_name);
    loginButton = (Button) findViewById(R.id.login_btn_login);
    loginButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    String clientId = nameView.getText().toString();
    initChatManager(clientId);
    ChatManager.getInstance().openClient(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (null == e) {
          Intent intent = new Intent(LoginActivity.this, MainActivity.class);
          startActivity(intent);
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
