package com.avoscloud.chat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.controller.ChatManager;

import butterknife.BindView;
import butterknife.OnClick;


public class EntryLoginActivity extends AVBaseActivity {

  @BindView(R.id.activity_login_et_username)
  public EditText userNameView;

  @BindView(R.id.activity_login_et_password)
  public EditText passwordView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.entry_login_activity);
  }

  @OnClick(R.id.activity_login_btn_login)
  public void onLoginClick(View v) {
      login();
  }

  @OnClick(R.id.activity_login_btn_register)
  public void onRegisterClick(View v) {
    Intent intent = new Intent(this, EntryRegisterActivity.class);
    startActivity(intent);
  }

  private void login() {
    final String name = userNameView.getText().toString().trim();
    final String password = passwordView.getText().toString().trim();

    if (TextUtils.isEmpty(name)) {
      Utils.toast(R.string.username_cannot_null);
      return;
    }

    if (TextUtils.isEmpty(password)) {
      Utils.toast(R.string.password_can_not_null);
      return;
    }

    final ProgressDialog dialog = showSpinnerDialog();
    LeanchatUser.logInInBackground(name, password, new LogInCallback<LeanchatUser>() {
      @Override
      public void done(LeanchatUser avUser, AVException e) {
        dialog.dismiss();
        if (filterException(e)) {
          imLogin();
        }
      }
    }, LeanchatUser.class);
  }

  /**
   * 因为 leancloud 实时通讯与账户体系是完全解耦的，所以此处需要先 LeanchatUser.logInInBackground
   * 如果验证账号密码成功，然后再 openClient 进行实时通讯
   */
  public void imLogin() {
    ChatManager.getInstance().openClient(this, LeanchatUser.getCurrentUserId(), new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if (filterException(e)) {
          Intent intent = new Intent(EntryLoginActivity.this, MainActivity.class);
          startActivity(intent);
          finish();
        }
      }
    });
  }
}
