package com.avoscloud.chat.activity;

import android.os.Bundle;
import com.avoscloud.chat.R;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;

/**
 * Created by lzw on 14-9-24.
 */
public class ProfileNotifySettingActivity extends AVBaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_setting_notify_layout);
    setTitle(R.string.profile_notifySetting);
  }
}
