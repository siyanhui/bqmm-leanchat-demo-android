package com.avoscloud.chat.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avoscloud.chat.R;
import com.avoscloud.chat.event.SearchUserItemClickEvent;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.leanchatlib.utils.PhotoUtils;
import com.avoscloud.leanchatlib.viewholder.CommonViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/3.
 */
public class SearchUserItemHolder extends CommonViewHolder<LeanchatUser> {

  private TextView nameView;
  private ImageView avatarView;
  private LeanchatUser leanchatUser;

  public SearchUserItemHolder(Context context, ViewGroup root) {
    super(context, root, R.layout.search_user_item_layout);

    nameView = (TextView)itemView.findViewById(R.id.search_user_item_tv_name);
    avatarView = (ImageView)itemView.findViewById(R.id.search_user_item_im_avatar);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EventBus.getDefault().post(new SearchUserItemClickEvent(leanchatUser.getObjectId()));
      }
    });
  }

  @Override
  public void bindData(final LeanchatUser leanchatUser) {
    this.leanchatUser = leanchatUser;
    ImageLoader.getInstance().displayImage(leanchatUser.getAvatarUrl(), avatarView, PhotoUtils.avatarImageOptions);
    nameView.setText(leanchatUser.getUsername());
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<SearchUserItemHolder>() {
    @Override
    public SearchUserItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new SearchUserItemHolder(parent.getContext(), parent);
    }
  };
}

