package com.avoscloud.leanchatlib.model;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by lzw on 14-9-26.
 */
public class Room {
  private AVIMMessage lastMessage;
  private AVIMConversation conversation;
  private String conversationId;
  private int unreadCount;

  public AVIMMessage getLastMessage() {
    return lastMessage;
  }

  public Date getLastMessageAt() {
    return ( null != conversation ? conversation.getLastMessageAt() : new Date());
  }

  public void setLastMessage(AVIMMessage lastMessage) {
    this.lastMessage = lastMessage;
  }

  public AVIMConversation getConversation() {
    return conversation;
  }

  public void setConversation(AVIMConversation conversation) {
    this.conversation = conversation;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }

  public static abstract class MultiRoomsCallback {
    public abstract void done(List<Room> rooms, AVException exception);
  }
}
