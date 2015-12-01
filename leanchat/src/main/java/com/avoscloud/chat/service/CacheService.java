package com.avoscloud.chat.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14/12/19.
 */
public class CacheService {
  private static volatile List<String> friendIds = new ArrayList<String>();


  public static List<String> getFriendIds() {
    return friendIds;
  }

  public static void setFriendIds(List<String> friendList) {
    friendIds.clear();
    if (friendList != null) {
        friendIds.addAll(friendList);
    }
  }
}
