package com.avoscloud.leanchatlib_demo;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by lzw on 15/4/27.
 */
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    AVOSCloud.initialize(this, "683jigxkqb10jrirelvd9vcn9ywbq2o436lfz1kngsvigm27",
      "ualzl8f8pxmryous77m3gf2z0dyhrhk6xdb7zkiu6flc0jxy");
    ChatManager.setDebugEnabled(true);// tag leanchatlib
    AVOSCloud.setDebugLogEnabled(true);  // set false when release
    initImageLoader(this);
    ChatManager.getInstance().init(this);
    ThirdPartUserUtils.setThirdPartUserProvider(new CustomUserProvider());
  }

  public static void initImageLoader(Context context) {
    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
      context)
      .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
        //.memoryCache(new WeakMemoryCache())
      .denyCacheImageMultipleSizesInMemory()
      .tasksProcessingOrder(QueueProcessingType.LIFO)
      .build();
    ImageLoader.getInstance().init(config);
  }
}
