package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avoscloud.leanchatlib.event.ConversationChangeEvent;
import com.avoscloud.leanchatlib.utils.Logger;

import java.util.List;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/1.
 */
public class ConversationEventHandler extends AVIMConversationEventHandler {

  private static ConversationEventHandler eventHandler;

  public static synchronized ConversationEventHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new ConversationEventHandler();
    }
    return eventHandler;
  }

  private ConversationEventHandler() {}

  @Override
  public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
    Logger.i(MessageHelper.nameByUserIds(members) + " left, kicked by " + MessageHelper.nameByUserId(kickedBy));
    refreshCacheAndNotify(conversation);
  }

  @Override
  public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
    Logger.i(MessageHelper.nameByUserIds(members) + " joined , invited by " + MessageHelper.nameByUserId(invitedBy));
    refreshCacheAndNotify(conversation);
  }

  private void refreshCacheAndNotify(AVIMConversation conversation) {
    ConversationChangeEvent conversationChangeEvent = new ConversationChangeEvent(conversation);
    EventBus.getDefault().post(conversationChangeEvent);
  }

  @Override
  public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
    Logger.i("you are kicked by " + MessageHelper.nameByUserId(kickedBy));
    refreshCacheAndNotify(conversation);
  }

  @Override
  public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
    Logger.i("you are invited by " + MessageHelper.nameByUserId(operator));
    refreshCacheAndNotify(conversation);
  }
}
