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
  static {
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("1", "1", "http://ac-x3o016bx.clouddn.com/CsaX0GuXL7gXWBkaBFXfBWZPlcanClEESzHxSq2T.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("2", "2", "http://ac-x3o016bx.clouddn.com/jUOhrGh3CkIaFwvf4ofNfl7YaBjWlmzSs6q8h4cQ.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("3", "3", "http://ac-x3o016bx.clouddn.com/FKnGDRxoy5UcZJWCrd1Tf51XkY4dfv6BvXR3TVOP.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("4", "4", "http://ac-x3o016bx.clouddn.com/7d6FwrxPGn1Xoym5QE6EU8PLay1FXyHQmO6cQiBw.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("5", "5", "http://ac-x3o016bx.clouddn.com/EHVl1ElC7JGmHQOrcDKaMKQDdeVZVzBJqHBDjqjZ.png"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("6", "6", "http://ac-x3o016bx.clouddn.com/wYerGOiBrWznlFMjp98UyVm1prS8DV1zand1rjLC.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("7", "7", "http://ac-x3o016bx.clouddn.com/PhNmVC496BirXdqH0uNfD9rgbp74eT4qBdX7diIl.jpg"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("8", "8", "http://ac-x3o016bx.clouddn.com/dqfZn3HVCwrNmnCnY4DZQ4ypvdsJN6iMeQHOuKZ2.png"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("9", "9", "http://ac-x3o016bx.clouddn.com/A907sNcLmnFECwqL7piOZjuhzah9IsYirreUfH8f.png"));
    partUsers.add(new ThirdPartUserUtils.ThirdPartUser("10", "10", "http://ac-x3o016bx.clouddn.com/gyYyrsnLdwaC7LHTZ538U51jKqKsZpbrteafNew9.png"));
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
  public void getFriends(ThirdPartUserUtils.FetchUserCallBack callBack) {
    callBack.done(partUsers, null);
  }
}
