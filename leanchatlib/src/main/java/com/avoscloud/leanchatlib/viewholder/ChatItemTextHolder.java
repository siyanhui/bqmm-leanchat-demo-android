package com.avoscloud.leanchatlib.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.EmotionHelper;
import com.avoscloud.leanchatlib.utils.Constants;
import com.melink.baseframe.bitmap.BitmapCreate;
import com.melink.baseframe.utils.DensityUtils;
import com.melink.baseframe.utils.StringUtils;
import com.melink.bqmmsdk.bean.Emoji;
import com.melink.bqmmsdk.sdk.BQMM;
import com.melink.bqmmsdk.sdk.BQMMMessageHelper;
import com.melink.bqmmsdk.sdk.IFetchEmojisByCodeListCallback;
import com.melink.bqmmsdk.ui.store.EmojiDetail;
import com.melink.bqmmsdk.widget.AnimatedGifDrawable;
import com.melink.bqmmsdk.widget.AnimatedImageSpan;
import com.melink.bqmmsdk.widget.GifMovieView;
import com.melink.bqmmsdk.widget.UpdateListener;
import com.thirdparty.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wli on 15/9/17.
 */
public class ChatItemTextHolder extends ChatItemHolder {

  protected TextView contentView;
  protected GifMovieView emojiView;

  public ChatItemTextHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft);
  }

  @Override
  public void initView() {
    super.initView();
    if (isLeft) {
      conventLayout.addView(View.inflate(getContext(), R.layout.chat_item_left_text_layout, null));
      contentView = (TextView) itemView.findViewById(R.id.chat_left_text_tv_content);
      emojiView = (GifMovieView) itemView.findViewById(R.id.emoji_view);
    } else {
      conventLayout.addView(View.inflate(getContext(), R.layout.chat_item_right_text_layout, null));
      contentView = (TextView) itemView.findViewById(R.id.chat_right_text_tv_content);
      emojiView = (GifMovieView) itemView.findViewById(R.id.emoji_view);
    }
  }

  @Override
  public void bindData(Object o) {
    super.bindData(o);
    AVIMMessage message = (AVIMMessage)o;
    if (message instanceof AVIMTextMessage) {
      AVIMTextMessage textMessage = (AVIMTextMessage) message;
      if (!TextUtils.isEmpty(textMessage.getText())) {
        if (textMessage.getAttrs() != null) {
          HashMap<String, Object> params = (HashMap) textMessage.getAttrs();
          if (params.get(Constants.TXT_MSGTYPE) != null) {
            setPic(getContext(), params);
              return;
          }
        }
          contentView.setVisibility(View.VISIBLE);
          emojiView.setVisibility(View.GONE);
          contentView.setText(EmotionHelper.replace(getContext(), textMessage.getText()));
      }
    }
  }

    private void setPic(final Context context, HashMap<String, Object> params) {
        String msgType = (String) params.get(Constants.TXT_MSGTYPE);
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(params.get(Constants.MSG_DATA).toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Glide.with(context).load(R.drawable.bqmm_emoji_loadfail).into(emojiView);
            return;
        }
        switch (msgType) {
            case Constants.FACETYPE:
                contentView.setVisibility(View.GONE);
                // 展示默认图片
                Glide.with(context).load(R.drawable.bqmm_emoji_loading).placeholder(R.drawable.bqmm_emoji_loading).into(emojiView);

                BQMM.getInstance().fetchBigEmojiByCodeList(context, BQMMMessageHelper.parseFaceMsgData(jsonArray), new IFetchEmojisByCodeListCallback() {
                    @Override
                    public void onSuccess(List<Emoji> emojis) {

                        final Emoji emoji = emojis.get(0);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            public void run() {
                                //holder.emojiView.setClickable(true);
                                // gif则按照gif展示，否则展示图片
                                emojiView.setVisibility(View.VISIBLE);
                                if (emoji.getMainImage().endsWith(".png")) {
                                    emojiView.setMovie(null);
                                    Glide.with(context).load(emoji.getMainImage()).placeholder(R.drawable.bqmm_emoji_loadfail).into(emojiView);
                                } else if (emoji.getMainImage().endsWith(".gif")) {
                                    emojiView.setVisibility(View.VISIBLE);
                                    if (emoji.getPathofImage() == null || emoji.getPathofImage().equals("")) {
                                        emojiView.setResource(StringUtils.decodestr(emoji.getMainImage()));// 读网络上的
                                    } else {
                                        emojiView.setMovieResourceByUri(emoji.getPathofImage());
                                    }
                                }
                                // 添加点击事件，跳转只详情预览,如果item的emoji还未请求下来，则跳转失效
                                emojiView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (emoji.getPackageId() == null || emoji.getPackageId().equals("")) {
                                            return;
                                        }
                                        Intent it = new Intent(context, EmojiDetail.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("Emoji_Detail", emoji);
                                        it.putExtras(bundle);
                                        context.startActivity(it);
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onError(Throwable arg0) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            public void run() {
                                Glide.with(context).load(R.drawable.bqmm_emoji_loadfail).into(emojiView);
                            }
                        });
                        return;
                    }
                });

                break;
            case Constants.EMOJITYPE:
                contentView.setVisibility(View.VISIBLE);
                emojiView.setVisibility(View.GONE);
                // 表情如果未下载，则下载表情进行展示
                contentView.setText("");
                showTextInfoFromStr(contentView, BQMMMessageHelper.parseMixedMsgData(jsonArray), context);
                break;

        }


    }

    private void showTextInfoFromStr(final TextView tv_chatcontent, final List<Object> messagecontent, final Context context) {
        final String content = BQMMMessageHelper.parseMixedMsgToString(messagecontent);
        if (!BQMMMessageHelper.isMixedMessage(messagecontent)) {
            tv_chatcontent.setText(content);
            return;
        }
        BQMM.getInstance().fetchSmallEmojiByCodeList(context, BQMMMessageHelper.findEmojiFormMixedMsg(messagecontent),
                new IFetchEmojisByCodeListCallback() {
                    @Override
                    public void onSuccess(final List<Emoji> emojis) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (emojis == null) {
                                    tv_chatcontent.setText(content);
                                    return;
                                }
                                showTextInfo(tv_chatcontent, BQMMMessageHelper.parseMixedMsg(content, emojis), context);
                            }
                        });
                    }

                    /**
                     * 当获取表情失败时，展示纯文本字符串
                     * @param arg0
                     */
                    @Override
                    public void onError(Throwable arg0) {
                        SpannableStringBuilder sb = new SpannableStringBuilder();
                        for (int i = 0; i < messagecontent.size(); i++) {
                            if (messagecontent.get(i).getClass().equals(Emoji.class)) {
                                Emoji item = (Emoji) messagecontent.get(i);
                                String tempText = "[" + item.getEmoCode() + "]";
                                sb.append(tempText);
                            } else {
                                sb.append(messagecontent.get(i).toString());
                            }
                        }
                        tv_chatcontent.setText(sb);
                    }
                });
    }


    private void showTextInfo(final TextView tv_chatcontent, List<Object> emojis, Context context) {
        // 根据返回的list集合实现图文混排
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int i = 0; i < emojis.size(); i++) {
            if (emojis.get(i).getClass().equals(Emoji.class)) {
                Emoji item = (Emoji) emojis.get(i);
                String tempText = "[" + item.getEmoCode() + "]";
                sb.append(tempText);
                // 此处需要判断，如果是非法Code，item的guid为空
                if (item.getGuid() != null && !item.getGuid().equals("null")) {
                    // 判断当前的Emoji对象是不是gif表情
                    if (item.getThumbail().endsWith(".png")) {
                        try {
                            Bitmap bit = BitmapCreate.bitmapFromFile(item.getPathofThumb(), DensityUtils.dip2px(context, 30), DensityUtils.dip2px(context, 30));
                            sb.setSpan(new ImageSpan(context, bit), sb.length() - tempText.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            // 判断缓存中是否已经有了这张图片?
                            InputStream is ;
                            is = new FileInputStream(item.getPathofImage());
                            sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is, item.getPathofImage(),
                                    new UpdateListener() {
                                        @Override
                                        public void update() {
                                            tv_chatcontent.postInvalidate();
                                        }
                                    })), sb.length() - tempText.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            is.close();
                        } catch (Exception e) {
                        }
                    }
                }
            } else {
                sb.append(emojis.get(i).toString());
            }
        }
        tv_chatcontent.setText(sb);

    }
}
