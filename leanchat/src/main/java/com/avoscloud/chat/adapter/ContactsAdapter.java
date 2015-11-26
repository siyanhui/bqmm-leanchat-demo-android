package com.avoscloud.chat.adapter;

import android.text.TextUtils;

import com.avoscloud.chat.viewholder.ContactItemHolder;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by wli on 15/11/24.
 */
public class ContactsAdapter extends HeaderListAdapter<ContactsAdapter.ContactItem> {

  /**
   * 在有序 memberList 中 MemberItem.sortContent 第一次出现时的字母与位置的 map
   */
  private Map<Character, Integer> indexMap = new HashMap<Character, Integer>();

  /**
   * 简体中文的 Collator
   */
  Collator cmp = Collator.getInstance(Locale.SIMPLIFIED_CHINESE);

  public ContactsAdapter() {
    super(ContactItemHolder.class);
  }


  /**
   * 设置成员列表，然后更新索引
   * 此处会对数据以 空格、数字、字母（汉字转化为拼音后的字母） 的顺序进行重新排列
   */
  public void setUserList(List<LeanchatUser> list) {
    List<ContactItem> contactList = new ArrayList<>();
    if (null != list) {
      for (LeanchatUser user : list) {
        ContactItem item = new ContactItem();
        item.user = user;
        item.sortContent = PinyinHelper.convertToPinyinString(user.getUsername(), "", PinyinFormat.WITHOUT_TONE);
        contactList.add(item);
      }
    }
    Collections.sort(contactList, new SortChineseName());
    indexMap = updateIndex(contactList);
    updateInitialsVisible(contactList);
    super.setDataList(contactList);
  }

  /**
   * 获取索引 Map
   */
  public Map<Character, Integer> getIndexMap() {
    return indexMap;
  }

  /**
   * 更新索引 Map
   */
  private Map<Character, Integer> updateIndex(List<ContactItem> list) {
    Character lastCharcter = '#';
    Map<Character, Integer> map = new HashMap<>();
    for (int i = 0; i < list.size(); i++) {
      Character curChar = Character.toLowerCase(list.get(i).sortContent.charAt(0));
      if (!lastCharcter.equals(curChar)) {
        map.put(curChar, i);
      }
      lastCharcter = curChar;
    }
    return map;
  }

  /**
   * 必须要排完序后，否则没意义
   * @param list
   */
  private void updateInitialsVisible(List<ContactItem> list) {
    if (null != list && list.size() > 0) {
      char lastInitial = ' ';
      for (ContactItem item : list) {
        if (!TextUtils.isEmpty(item.sortContent)) {
          item.initialVisible = (lastInitial != item.sortContent.charAt(0));
          lastInitial = item.sortContent.charAt(0);
        } else {
          item.initialVisible = true;
          lastInitial = ' ';
        }
      }
    }
  }

  public class SortChineseName implements Comparator<ContactItem> {

    @Override
    public int compare(ContactItem str1, ContactItem str2) {
      if (null == str1) {
        return -1;
      }
      if (null == str2) {
        return 1;
      }
      if (cmp.compare(str1.sortContent, str2.sortContent) > 0) {
        return 1;
      }else if (cmp.compare(str1.sortContent, str2.sortContent) < 0) {
        return -1;
      }
      return 0;
    }
  }

  public static class ContactItem {
    public LeanchatUser user;
    public String sortContent;
    public boolean initialVisible;
  }
}