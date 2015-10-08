package com.avoscloud.leanchatlib.controller;

import java.util.List;

/**
 * 配置用户信息和通知处理
 * TODO 此类稍后会去掉
 */
public interface ChatManagerAdapter {

  /**
   * 为了支持能够同步获取用户信息，请先缓存用户信息，会在后台线程调用此函数
   * @param userIds 将可能被 getUserInfoById() 用到的userId，也即聊天页面消息的发送者们
   * @throws Exception 可抛出网络异常
   */
  void cacheUserInfoByIdsInBackground(List<String> userIds) throws Exception;
}
