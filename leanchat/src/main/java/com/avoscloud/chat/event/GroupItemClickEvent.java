package com.avoscloud.chat.event;

/**
 * Created by wli on 15/11/27.
 */
public class GroupItemClickEvent {
  public String conversationId;
  public GroupItemClickEvent(String id) {
    conversationId = id;
  }
}
