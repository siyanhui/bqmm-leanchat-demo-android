package com.avoscloud.leanchatlib_demo;

import android.content.res.Resources;

import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wli on 15/12/4.
 */
public class CustomUserProvider implements ThirdPartUserUtils.ThirdPartDataProvider {

  private static List<ThirdPartUserUtils.ThirdPartUser> partUsers = new ArrayList<ThirdPartUserUtils.ThirdPartUser>();

  private static String[] avatarList = new String[] {
    "http://ac-x3o016bx.clouddn.com/CsaX0GuXL7gXWBkaBFXfBWZPlcanClEESzHxSq2T.jpg",
    "http://ac-x3o016bx.clouddn.com/jUOhrGh3CkIaFwvf4ofNfl7YaBjWlmzSs6q8h4cQ.jpg",
    "http://ac-x3o016bx.clouddn.com/FKnGDRxoy5UcZJWCrd1Tf51XkY4dfv6BvXR3TVOP.jpg",
    "http://ac-x3o016bx.clouddn.com/7d6FwrxPGn1Xoym5QE6EU8PLay1FXyHQmO6cQiBw.jpg",
    "http://ac-x3o016bx.clouddn.com/EHVl1ElC7JGmHQOrcDKaMKQDdeVZVzBJqHBDjqjZ.png",
    "http://ac-x3o016bx.clouddn.com/wYerGOiBrWznlFMjp98UyVm1prS8DV1zand1rjLC.jpg",
    "http://ac-x3o016bx.clouddn.com/PhNmVC496BirXdqH0uNfD9rgbp74eT4qBdX7diIl.jpg",
    "http://ac-x3o016bx.clouddn.com/dqfZn3HVCwrNmnCnY4DZQ4ypvdsJN6iMeQHOuKZ2.png",
    "http://ac-x3o016bx.clouddn.com/A907sNcLmnFECwqL7piOZjuhzah9IsYirreUfH8f.png",
    "http://ac-x3o016bx.clouddn.com/gyYyrsnLdwaC7LHTZ538U51jKqKsZpbrteafNew9.png"
  };
  static {
    for (int i = 0; i < 36; i++) {
      partUsers.add(new ThirdPartUserUtils.ThirdPartUser(i + "", "user_" + i, avatarList[i%10]));
    }
  }

  @Override
  public ThirdPartUserUtils.ThirdPartUser getSelf() {
    return new ThirdPartUserUtils.ThirdPartUser("daweibayu", "daweibayu",
      "http://ac-x3o016bx.clouddn.com/CsaX0GuXL7gXWBkaBFXfBWZPlcanClEESzHxSq2T.jpg");
  }

  @Override
  public void getFriend(String userId, ThirdPartUserUtils.FetchUserCallBack callBack) {
    for (ThirdPartUserUtils.ThirdPartUser user : partUsers) {
      if (user.userId.equals(userId)) {
        callBack.done(Arrays.asList(user), null);
        return;
      }
    }
    callBack.done(null, new Resources.NotFoundException("not found this user"));
  }

  @Override
  public void getFriends(List<String> list, ThirdPartUserUtils.FetchUserCallBack callBack) {
    List<ThirdPartUserUtils.ThirdPartUser> userList = new ArrayList<ThirdPartUserUtils.ThirdPartUser>();
    for (String userId : list) {
      for (ThirdPartUserUtils.ThirdPartUser user : partUsers) {
        if (user.userId.equals(userId)) {
          userList.add(user);
          break;
        }
      }
    }
    callBack.done(userList, null);
  }

  @Override
  public void getFriends(int skip, int limit, ThirdPartUserUtils.FetchUserCallBack callBack) {
    int begin = partUsers.size() > skip ? skip : partUsers.size();
    int end = partUsers.size() > skip + limit ? skip + limit : partUsers.size();
    callBack.done(partUsers.subList(begin, end), null);
  }
}
