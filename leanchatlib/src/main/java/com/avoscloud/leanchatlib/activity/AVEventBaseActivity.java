package com.avoscloud.leanchatlib.activity;

import android.os.Bundle;

import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/7/24.
 */
public class AVEventBaseActivity extends AVBaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }
}
