package com.avoscloud.leanchatlib_demo;

/**
 * Created by wli on 15/11/26.
 */
public class ContactItemClickEvent {

  public String memberId;
  public boolean isLongClick;
  public ContactItemClickEvent(String id, boolean isLongClick) {
    memberId = id;
    this.isLongClick = isLongClick;
  }
}
