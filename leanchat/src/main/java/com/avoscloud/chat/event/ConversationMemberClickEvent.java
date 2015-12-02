package com.avoscloud.chat.event;

/**
 * Created by wli on 15/12/2.
 */
public class ConversationMemberClickEvent {
  public String memberId;
  public boolean isLongClick;

  public ConversationMemberClickEvent(String memberId, boolean isLongClick) {
    this.memberId = memberId;
    this.isLongClick = isLongClick;
  }
}
