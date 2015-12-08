package com.avoscloud.leanchatlib_demo;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avoscloud.leanchatlib.activity.AVChatActivity;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.avoscloud.leanchatlib.viewholder.CommonViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/11/24.
 */
public class ContactItemHolder extends CommonViewHolder<ThirdPartUserUtils.ThirdPartUser> {

  TextView nameView;
  ImageView avatarView;

  public ThirdPartUserUtils.ThirdPartUser thirdPartUser;

  public ContactItemHolder(Context context, ViewGroup root) {
    super(context, root, R.layout.common_user_item);
    initView();
  }

  public void initView() {
    nameView = (TextView)itemView.findViewById(R.id.tv_friend_name);
    avatarView = (ImageView)itemView.findViewById(R.id.img_friend_avatar);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), AVChatActivity.class);
        intent.putExtra(Constants.MEMBER_ID, thirdPartUser.userId);
        getContext().startActivity(intent);
      }
    });
  }

  @Override
  public void bindData(ThirdPartUserUtils.ThirdPartUser thirdPartUser) {
    this.thirdPartUser = thirdPartUser;
    ImageLoader.getInstance().displayImage(thirdPartUser.avatarUrl,
      avatarView, com.avoscloud.leanchatlib.utils.PhotoUtils.avatarImageOptions);
    nameView.setText(thirdPartUser.name);
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<ContactItemHolder>() {
    @Override
    public ContactItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new ContactItemHolder(parent.getContext(), parent);
    }
  };
}
