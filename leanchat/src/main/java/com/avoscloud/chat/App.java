package com.avoscloud.chat;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avoscloud.chat.friends.AddRequest;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.model.UpdateInfo;
import com.avoscloud.chat.service.PushManager;
import com.avoscloud.chat.util.LeanchatUserProvider;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.baidu.mapapi.SDKInitializer;
import com.melink.bqmmsdk.sdk.BQMM;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;


/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static boolean debug = true;
  public static App ctx;

  @Override
  public void onCreate() {
    super.onCreate();
    ctx = this;
    Utils.fixAsyncTaskBug();

    String appId = "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g";
    String appKey = "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78";

    LeanchatUser.alwaysUseSubUserClass(LeanchatUser.class);
    AVObject.registerSubclass(AddRequest.class);
    AVObject.registerSubclass(UpdateInfo.class);

    AVOSCloud.initialize(this, appId, appKey);

    /**
     * BQMM集成
     * 初始化BQMM
     */
    BQMM.getInstance().initConfig(getApplicationContext(), "d494bd2fb60048f0adc3305613dc7ca2", "9d512fd35e2c498aa1f63c0fc9dadf80");
    // 节省流量
    AVOSCloud.setLastModifyEnabled(true);

    PushManager.getInstance().init(ctx);
    AVOSCloud.setDebugLogEnabled(debug);
    AVAnalytics.enableCrashReport(this, !debug);
    initImageLoader(ctx);
    initBaiduMap();
    if (App.debug) {
      openStrictMode();
    }

    ThirdPartUserUtils.setThirdPartUserProvider(new LeanchatUserProvider());
    ChatManager.getInstance().init(this);
    ChatManager.getInstance().setDebugEnabled(App.debug);
  }

  public void openStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
            //.penaltyDeath()
        .build());
  }

  /**
   * 初始化ImageLoader
   */
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

  private void initBaiduMap() {
    SDKInitializer.initialize(this);
  }
}
