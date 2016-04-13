package com.avoscloud.leanchatlib.event;

import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

/**
 * Created by fantasy on 16/4/12.
 */
public class InputBottomBarEmojiEvent extends InputBottomBarEvent {
    public AVIMTextMessage avimTextMessage;

    public InputBottomBarEmojiEvent(int action, AVIMTextMessage content, Object tag) {
        super(action, tag);
        avimTextMessage = content;
    }
}
