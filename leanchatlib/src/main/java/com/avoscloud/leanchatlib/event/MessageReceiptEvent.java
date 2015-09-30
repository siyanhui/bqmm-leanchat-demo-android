package com.avoscloud.leanchatlib.event;

import com.avos.avoscloud.im.v2.AVIMTypedMessage;

/**
 * Created by wli on 15/9/23.
 * 此处仅仅在 ConversationRecentFragment 使用，其他地方的更细都走回调
 */
public class MessageReceiptEvent {
  public enum Type {
    Come, Receipt
  }

  private AVIMTypedMessage message;
  private Type type;

  public MessageReceiptEvent(AVIMTypedMessage message, Type type) {
    this.message = message;
    this.type = type;
  }

  public AVIMTypedMessage getMessage() {
    return message;
  }

  public Type getType() {
    return type;
  }
}