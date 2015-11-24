package com.avoscloud.chat.viewholder;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVGeoPoint;
import com.avoscloud.chat.App;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.viewholder.CommonViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by wli on 15/11/24.
 */
public class DiscoverItemHolder extends CommonViewHolder<LeanchatUser> {

  private static final double EARTH_RADIUS = 6378137;
  PrettyTime prettyTime;
  AVGeoPoint location;

  TextView nameView;
  TextView distanceView;
  TextView loginTimeView;
  ImageView avatarView;

  public DiscoverItemHolder(Context context, ViewGroup root) {
    super(context, root, R.layout.discover_near_people_item);

    prettyTime = new PrettyTime();
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(context);
    location = preferenceMap.getLocation();

    initView();
  }

  protected void initView() {
    nameView = (TextView)itemView.findViewById(R.id.name_text);
    distanceView = (TextView)itemView.findViewById(R.id.distance_text);
    loginTimeView = (TextView)itemView.findViewById(R.id.login_time_text);
    avatarView = (ImageView)itemView.findViewById(R.id.avatar_view);
  }

  @Override
  public void bindData(LeanchatUser user) {
    if (null != user) {
      ImageLoader.getInstance().displayImage(user.getAvatarUrl(), avatarView, com.avoscloud.leanchatlib.utils.PhotoUtils.avatarImageOptions);
      AVGeoPoint geoPoint = user.getAVGeoPoint(LeanchatUser.LOCATION);
      String currentLat = String.valueOf(location.getLatitude());
      String currentLong = String.valueOf(location.getLongitude());
      if (geoPoint != null && !currentLat.equals("") && !currentLong.equals("")) {
        double distance = DistanceOfTwoPoints(Double.parseDouble(currentLat), Double.parseDouble(currentLong),
        user.getAVGeoPoint(LeanchatUser.LOCATION).getLatitude(),
        user.getAVGeoPoint(LeanchatUser.LOCATION).getLongitude());
        distanceView.setText(Utils.getPrettyDistance(distance));
      } else {
        distanceView.setText(App.ctx.getString(R.string.discover_unknown));
      }
      nameView.setText(user.getUsername());
      Date updatedAt = user.getUpdatedAt();
      String prettyTimeStr = this.prettyTime.format(updatedAt);
      loginTimeView.setText(App.ctx.getString(R.string.discover_recent_login_time) + prettyTimeStr);
    } else {
      nameView.setText("");
      distanceView.setText("");
      loginTimeView.setText("");
      avatarView.setImageResource(0);
    }
  }

  /**
   * 根据两点间经纬度坐标（double值），计算两点间距离，
   *
   * @param lat1
   * @param lng1
   * @param lat2
   * @param lng2
   * @return 距离：单位为米
   */
  public static double DistanceOfTwoPoints(double lat1, double lng1,
                                           double lat2, double lng2) {
    double radLat1 = rad(lat1);
    double radLat2 = rad(lat2);
    double a = radLat1 - radLat2;
    double b = rad(lng1) - rad(lng2);
    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
      + Math.cos(radLat1) * Math.cos(radLat2)
      * Math.pow(Math.sin(b / 2), 2)));
    s = s * EARTH_RADIUS;
    s = Math.round(s * 10000) / 10000;
    return s;
  }

  private static double rad(double d) {
    return d * Math.PI / 180.0;
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<DiscoverItemHolder>() {
    @Override
    public DiscoverItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new DiscoverItemHolder(parent.getContext(), parent);
    }
  };
}
