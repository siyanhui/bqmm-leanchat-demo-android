package com.avoscloud.chat.service;

import android.content.Context;
import com.avoscloud.leanchatlib.controller.ChatManagerAdapter;

import java.util.List;

/**
 * Created by lzw on 15/5/13.
 * TODO 此类稍后会去掉
 */
public class ChatManagerAdapterImpl implements ChatManagerAdapter {
  private Context context;

  public ChatManagerAdapterImpl(Context context) {
    this.context = context;
  }

  @Override
  public void cacheUserInfoByIdsInBackground(List<String> userIds) throws Exception {
    CacheService.cacheUsers(userIds);
  }
}
