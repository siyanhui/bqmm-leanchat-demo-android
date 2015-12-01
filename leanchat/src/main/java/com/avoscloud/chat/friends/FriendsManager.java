package com.avoscloud.chat.friends;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.UserCacheUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wli on 15/12/1.
 */
class FriendsManager {

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

  public static void fetchFriends(boolean isForce, final FindCallback findCallback) {
    AVQuery.CachePolicy policy =
      (isForce ? AVQuery.CachePolicy.NETWORK_ELSE_CACHE : AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
    LeanchatUser.getCurrentUser().findFriendsWithCachePolicy(policy, new FindCallback<LeanchatUser>() {
      @Override
      public void done(List<LeanchatUser> list, AVException e) {
        if (null != e) {
          findCallback.done(null, e);
        } else {
          final List<String> userIds = new ArrayList<String>();
          for (LeanchatUser user : list) {
            userIds.add(user.getObjectId());
          }
          UserCacheUtils.fetchUsers(userIds, new UserCacheUtils.CacheUserCallback() {
            @Override
            public void done(Exception e) {
              setFriendIds(userIds);
              findCallback.done(UserCacheUtils.getUsersFromCache(userIds), null);
            }
          });

        }
      }
    });
  }
}
