package com.avoscloud.leanchatlib.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.utils.Utils;
import com.avoscloud.leanchatlib.view.PlayButton;

import java.io.File;

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
    if (message instanceof AVIMAudioMessage) {
      AVIMAudioMessage audioMessage = (AVIMAudioMessage)message;
      playButton.setLeftSide(!MessageHelper.fromMe(audioMessage));
      playButton.setPath(MessageHelper.getFilePath(audioMessage));

      //TODO 应该是点击再加载
      File file = new File(MessageHelper.getFilePath(audioMessage));
      if (!file.exists()) {
        String url = audioMessage.getFileUrl();
        Utils.downloadFileIfNotExists(url, file);
      }
    }
  }
}