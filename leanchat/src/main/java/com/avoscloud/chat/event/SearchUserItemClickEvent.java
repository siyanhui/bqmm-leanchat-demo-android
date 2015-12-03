package com.avoscloud.chat.event;

/**
 * Created by wli on 15/12/3.
 */
public class SearchUserItemClickEvent {
  public String memberId;
  public SearchUserItemClickEvent(String memberId) {
    this.memberId = memberId;
  }
}
