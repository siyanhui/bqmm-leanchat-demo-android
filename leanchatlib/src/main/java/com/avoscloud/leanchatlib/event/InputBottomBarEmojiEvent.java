package com.avoscloud.leanchatlib.event;

import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

/**
 * BQMM集成
 * 用于增加表情输入事件
 */
public class InputBottomBarEmojiEvent extends InputBottomBarEvent {
    public AVIMTextMessage avimTextMessage;

    public InputBottomBarEmojiEvent(int action, AVIMTextMessage content, Object tag) {
        super(action, tag);
        avimTextMessage = content;
    }
}
