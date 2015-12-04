package com.avoscloud.leanchatlib_demo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.event.ConversationItemClickEvent;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.utils.PhotoUtils;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.avoscloud.leanchatlib.viewholder.CommonViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/10/8.
 */
public class ConversationItemHolder extends CommonViewHolder {

  ImageView recentAvatarView;
  TextView recentNameView;
  TextView recentMsgView;
  TextView recentTimeView;
  TextView recentUnreadView;

  public ConversationItemHolder(ViewGroup root) {
    super(root.getContext(), root, R.layout.conversation_item);
    initView();
  }

  public void initView() {
    recentAvatarView = (ImageView)itemView.findViewById(R.id.iv_recent_avatar);
    recentNameView = (TextView)itemView.findViewById(R.id.recent_time_text);
    recentMsgView = (TextView)itemView.findViewById(R.id.recent_msg_text);
    recentTimeView = (TextView)itemView.findViewById(R.id.recent_teim_text);
    recentUnreadView = (TextView)itemView.findViewById(R.id.recent_unread);
  }


  @Override
  public void bindData(Object o) {
    final Room room = (Room) o;
    AVIMConversation conversation = room.getConversation();
    if (null != conversation) {
      if (ConversationHelper.typeOfConversation(conversation) == ConversationType.Single) {
        String userId = ConversationHelper.otherIdOfConversation(conversation);
        ImageLoader.getInstance().displayImage(ThirdPartUserUtils.getInstance().getUserAvatar(userId), recentAvatarView, PhotoUtils.avatarImageOptions);
      } else {
//        recentAvatarView.setImageBitmap(ConversationManager.getConversationIcon(conversation));
      }
      recentNameView.setText(ConversationHelper.nameOfConversation(conversation));

      int num = room.getUnreadCount();
      if (num > 0) {
        recentUnreadView.setVisibility(View.VISIBLE);
        recentUnreadView.setText(num + "");
      } else {
        recentUnreadView.setVisibility(View.GONE);
      }

      if (room.getLastMessage() != null) {
        Date date = new Date(room.getLastMessage().getTimestamp());
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        recentTimeView.setText(format.format(date));

        //TODO 此处并不一定是 AVIMTypedMessage
        recentMsgView.setText(MessageHelper.outlineOfMsg((AVIMTypedMessage) room.getLastMessage()));
      }

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          ConversationItemClickEvent itemClickEvent = new ConversationItemClickEvent();
          itemClickEvent.conversationId = room.getConversationId();
          EventBus.getDefault().post(itemClickEvent);
        }
      });
    }
  }

  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<ConversationItemHolder>() {
    @Override
    public ConversationItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new ConversationItemHolder(parent);
    }
  };
}
