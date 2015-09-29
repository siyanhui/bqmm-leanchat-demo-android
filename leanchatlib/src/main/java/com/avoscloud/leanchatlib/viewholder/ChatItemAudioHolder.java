package com.avoscloud.leanchatlib.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.utils.LocalCacheUtils;
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
    AVIMAudioMessage message = (AVIMAudioMessage)o;
    if (message instanceof AVIMAudioMessage) {
      AVIMAudioMessage audioMessage = (AVIMAudioMessage)message;
      playButton.setLeftSide(!MessageHelper.fromMe(audioMessage));
      playButton.setPath(MessageHelper.getFilePath(audioMessage));
      LocalCacheUtils.downloadFileAsync(audioMessage.getFileUrl(), MessageHelper.getFilePath(audioMessage));
    }
  }
}