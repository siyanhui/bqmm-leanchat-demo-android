package com.avoscloud.chat.entity.avobject;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;

public class User {
  public static final String USERNAME = "username";
  public static final String AVATAR = "avatar";
  public static final String LOCATION = "location";
  public static final String INSTALLATION = "installation";


  public static String getCurrentUserId() {
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      return user.getObjectId();
    } else {
      return null;
    }
  }

  public static String getAvatarUrl(AVUser user) {
    AVFile avatar = user.getAVFile(AVATAR);
    if (avatar != null) {
      return avatar.getUrl();
    } else {
      return null;
    }
  }
}
