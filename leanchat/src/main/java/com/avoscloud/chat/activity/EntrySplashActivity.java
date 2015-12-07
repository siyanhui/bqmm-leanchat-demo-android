package com.avoscloud.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.avoscloud.chat.R;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;

public class EntrySplashActivity extends AVBaseActivity {
  public static final int SPLASH_DURATION = 2000;
  private static final int GO_MAIN_MSG = 1;
  private static final int GO_LOGIN_MSG = 2;

  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case GO_MAIN_MSG:
          MainActivity.goMainActivityFromActivity(EntrySplashActivity.this);
          finish();
          break;
        case GO_LOGIN_MSG:
          Intent intent = new Intent(EntrySplashActivity.this, EntryLoginActivity.class);
          EntrySplashActivity.this.startActivity(intent);
          finish();
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.entry_splash_layout);
    if (LeanchatUser.getCurrentUser() != null) {
      LeanchatUser.getCurrentUser().updateUserInfo();
      handler.sendEmptyMessageDelayed(GO_MAIN_MSG, SPLASH_DURATION);
    } else {
      handler.sendEmptyMessageDelayed(GO_LOGIN_MSG, SPLASH_DURATION);
    }
  }
}
