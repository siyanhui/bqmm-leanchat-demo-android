package com.avoscloud.leanchatlib.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.view.PlayButton;

/**
 * Created by wli on 15/9/17.
 */
public class ChatItemAudioHolder extends ChatItemHolder {

  protected PlayButton playButton;

  public ChatItemAudioHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    conventLayout.addView(View.inflate(getContext(), R.layout.chat_item_audio, null));
    playButton = (PlayButton) itemView.findViewById(R.id.playBtn);
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    AVIMMessage message = (AVIMMessage)o;
    String content =  getContext().getString(R.string.unspport_message_type);
    if (message instanceof AVIMTypedMessage) {
      AVIMTypedMessage typedMessage = (AVIMTypedMessage)message;
      playButton.setLeftSide(!MessageHelper.fromMe(typedMessage));
      playButton.setPath(MessageHelper.getFilePath(typedMessage));
    }
  }
}