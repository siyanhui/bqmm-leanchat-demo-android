package com.avoscloud.leanchatlib.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.adapter.ChatMessageAdapter;
import com.avoscloud.leanchatlib.controller.AVIMTypedMessagesArrayCallback;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageAgent;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.controller.RoomsTable;
import com.avoscloud.leanchatlib.event.InputBottomBarEvent;
import com.avoscloud.leanchatlib.event.InputBottomBarRecordEvent;
import com.avoscloud.leanchatlib.event.InputBottomBarTextEvent;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.MessageEvent;
import com.avoscloud.leanchatlib.utils.LogUtils;
import com.avoscloud.leanchatlib.utils.PathUtils;
import com.avoscloud.leanchatlib.utils.ProviderPathUtils;
import com.avoscloud.leanchatlib.utils.Utils;
import com.avoscloud.leanchatlib.view.RefreshableView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatActivity extends AVEventBaseActivity {
  public static final String CONVID = "convid";
  private static final int PAGE_SIZE = 15;
  private static final int TAKE_CAMERA_REQUEST = 2;
  private static final int GALLERY_REQUEST = 0;
  private static final int GALLERY_KITKAT_REQUEST = 3;

  protected ConversationType conversationType;
  protected AVIMConversation conversation;
  protected MessageAgent messageAgent;
  protected MessageAgent.SendCallback defaultSendCallback = new DefaultSendCallback();
  protected ChatManager chatManager = ChatManager.getInstance();
  protected ChatMessageAdapter adapter;
  protected RoomsTable roomsTable;
  protected RefreshableView refreshableView;
  protected ListView messageListView;
  protected InputBottomBar inputBottomBar;
  protected String localCameraPath = PathUtils.getPicturePathByCurrentTime();
  protected boolean isLoadingMessages = false;


  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_layout);
    commonInit();
    findView();
    initListView();

    initByIntent(getIntent());
  }

  private void findView() {
    refreshableView = (RefreshableView) findViewById(R.id.chat_layout_refreshableview);
    messageListView = (ListView) findViewById(R.id.chat_layout_lv_message);
    inputBottomBar = (InputBottomBar) findViewById(R.id.chat_layout_inputbottombar);
  }

  private void initByIntent(Intent intent) {
    initData(intent);
    loadMessagesWhenInit(PAGE_SIZE);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    initByIntent(intent);
  }

  private void initListView() {
    refreshableView.setRefreshListener(new RefreshableView.ListRefreshListener(messageListView) {
      @Override
      public void onRefresh() {
        loadOldMessages();
      }
    });
    messageListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
  }

  void commonInit() {
    roomsTable = ChatManager.getInstance().getRoomsTable();
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  private boolean isConversationEmpty(AVIMConversation conversation) {
    if (conversation == null) {
      showToast("未找到对话，请退出重试。请检查是否调用了 ChatManager.registerConversation()");
      this.finish();
      return true;
    }
    return false;
  }

  public void initData(Intent intent) {
    String convid = intent.getStringExtra(CONVID);
    conversation = chatManager.lookUpConversationById(convid);
    if (isConversationEmpty(conversation)) {
      return;
    }
    initActionBar(ConversationHelper.titleOfConversation(conversation));
    messageAgent = new MessageAgent(conversation);
    messageAgent.setSendCallback(defaultSendCallback);
    roomsTable.clearUnread(conversation.getConversationId());
    conversationType = ConversationHelper.typeOfConversation(conversation);
    bindAdapterToListView(conversationType);
  }

  protected void initActionBar(String title) {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      if (title != null) {
        actionBar.setTitle(title);
      }
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
    } else {
      LogUtils.i("action bar is null, so no title, please set an ActionBar style for activity");
    }
  }

  private void bindAdapterToListView(ConversationType conversationType) {
    adapter = new ChatMessageAdapter(this, conversationType);
    adapter.setClickListener(new ChatMessageAdapter.ClickListener() {
      @Override
      public void onFailButtonClick(AVIMTypedMessage msg) {
        messageAgent.resendMessage(msg, new MessageAgent.SendCallback() {
          @Override
          public void onStart(AVIMTypedMessage message) {

          }

          @Override
          public void onError(AVIMTypedMessage message, Exception e) {
            LogUtils.d("resend message error");
            // 应该只重新加载一条, Todo
            loadMessagesWhenInit(adapter.getCount());
          }

          @Override
          public void onSuccess(AVIMTypedMessage message) {
            LogUtils.d("resend message success");
            // 应该只重新加载一条, Todo
            loadMessagesWhenInit(adapter.getCount());
          }
        });
      }

      @Override
      public void onLocationViewClick(AVIMLocationMessage locMsg) {
        onLocationMessageViewClicked(locMsg);
      }

      @Override
      public void onImageViewClick(AVIMImageMessage imageMsg) {
        onImageMessageViewClicked(imageMsg, MessageHelper.getFilePath(imageMsg));
      }
    });
    messageListView.setAdapter(adapter);
  }

  public void selectImageFromLocal() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.chat_activity_select_picture)),
          GALLERY_REQUEST);
    } else {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("image/*");
      startActivityForResult(intent, GALLERY_KITKAT_REQUEST);
    }
  }

  public void selectImageFromCamera() {
    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    Uri imageUri = Uri.fromFile(new File(localCameraPath));
    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, TAKE_CAMERA_REQUEST);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        super.onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case GALLERY_REQUEST:
        case GALLERY_KITKAT_REQUEST:
          if (intent == null) {
            showToast("return intent is null");
            return;
          }
          Uri uri;
          if (requestCode == GALLERY_REQUEST) {
            uri = intent.getData();
          } else {
            //for Android 4.4
            uri = intent.getData();
            final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
          }
          String localSelectPath = ProviderPathUtils.getPath(this, uri);
          messageAgent.sendImage(localSelectPath);
          inputBottomBar.hideMoreLayout();
          break;
        case TAKE_CAMERA_REQUEST:
          messageAgent.sendImage(localCameraPath);
          inputBottomBar.hideMoreLayout();
          break;
      }
    }
  }

  public void scrollToLast() {
    messageListView.post(new Runnable() {
      @Override
      public void run() {
        messageListView.smoothScrollToPosition(messageListView.getAdapter().getCount() - 1);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private AVIMTypedMessage findMessage(String messageId) {
    for (AVIMTypedMessage originMessage : adapter.getDatas()) {
      if (originMessage.getMessageId() != null && originMessage.getMessageId().equals(messageId)) {
        return originMessage;
      }
    }
    return null;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isConversationEmpty(conversation)) {
      return;
    }
    ChatManager.setCurrentChattingConvid(conversation.getConversationId());
  }

  @Override
  protected void onPause() {
    super.onPause();
    roomsTable.clearUnread(conversation.getConversationId());
    ChatManager.setCurrentChattingConvid(null);
  }

  public void loadMessagesWhenInit(int limit) {
    if (isLoadingMessages) {
      return;
    }
    isLoadingMessages = true;
    ChatManager.getInstance().queryMessages(conversation, null, 0, limit, new
        AVIMTypedMessagesArrayCallback() {
          @Override
          public void done(final List<AVIMTypedMessage> typedMessages, AVException e) {
            if (filterException(e)) {
              new CacheMessagesTask(ChatActivity.this, typedMessages) {
                @Override
                void onPostRun(List<AVIMTypedMessage> messages, Exception e) {
                  if (filterException(e)) {
                    adapter.setDatas(typedMessages);
                    adapter.notifyDataSetChanged();
                    scrollToLast();
                  }
                  isLoadingMessages = false;
                }
              }.execute();
            } else {
              isLoadingMessages = false;
            }
          }
        });
  }

  public abstract class CacheMessagesTask extends AsyncTask<Void, Void, Void> {
    private List<AVIMTypedMessage> messages;
    private volatile Exception e;

    public CacheMessagesTask(Context context, List<AVIMTypedMessage> messages) {
      this.messages = messages;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      Set<String> userIds = new HashSet<>();
      for (AVIMTypedMessage msg : messages) {
        AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
        if (type == AVIMReservedMessageType.AudioMessageType) {
          File file = new File(MessageHelper.getFilePath(msg));
          if (!file.exists()) {
            AVIMAudioMessage audioMsg = (AVIMAudioMessage) msg;
            String url = audioMsg.getFileUrl();
//            if (audioMsg.getFileMetaData() != null) {
//              int size = (Integer) audioMsg.getFileMetaData().get("size");
//              LogUtils.d("metaData size", size + "");
//            }
            Utils.downloadFileIfNotExists(url, file);
          }
        }
        userIds.add(msg.getFrom());
      }
      if (chatManager.getChatManagerAdapter() == null) {
        throw new IllegalStateException("please set ChatManagerAdapter in ChatManager to provide userInfo");
      }
      try {
        chatManager.getChatManagerAdapter().cacheUserInfoByIdsInBackground(new ArrayList<String>(userIds));
      } catch (Exception e1) {
        LogUtils.logException(e1);
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      onPostRun(messages, e);
    }

    abstract void onPostRun(List<AVIMTypedMessage> messages, Exception e);
  }

  public void loadOldMessages() {
    if (adapter.getDatas().size() == 0) {
      refreshableView.finishRefreshing();
      return;
    } else {
      if (isLoadingMessages) {
        refreshableView.finishRefreshing();
        LogUtils.i("It's loading messages, so ignore load again.");
        return;
      }
      isLoadingMessages = true;
      AVIMTypedMessage firstMsg = adapter.getDatas().get(0);
      String msgId = firstMsg.getMessageId();
      long time = firstMsg.getTimestamp();
      ChatManager.getInstance().queryMessages(conversation, msgId, time, PAGE_SIZE, new AVIMTypedMessagesArrayCallback() {
        @Override
        public void done(List<AVIMTypedMessage> typedMessages, AVException e) {
          refreshableView.finishRefreshing();
          if (filterException(e)) {
            new CacheMessagesTask(ChatActivity.this, typedMessages) {
              @Override
              void onPostRun(List<AVIMTypedMessage> typedMessages, Exception e) {
                if (filterException(e)) {
                  List<AVIMTypedMessage> newMessages = new ArrayList<>(PAGE_SIZE);
                  newMessages.addAll(typedMessages);
                  newMessages.addAll(adapter.getDatas());
                  adapter.setDatas(newMessages);
                  adapter.notifyDataSetChanged();
                  if (typedMessages.size() > 0) {
                    messageListView.setSelection(typedMessages.size() - 1);
                  } else {
                    showToast(R.string.chat_activity_loadMessagesFinish);
                  }
                }
                isLoadingMessages = false;
              }
            }.execute();
          } else {
            isLoadingMessages = false;
          }
        }
      });
    }

  }

  class DefaultSendCallback implements MessageAgent.SendCallback {

    @Override
    public void onStart(AVIMTypedMessage message) {

    }

    @Override
    public void onError(AVIMTypedMessage message, Exception e) {
      LogUtils.i();
      addMessageAndScroll(message);
    }

    @Override
    public void onSuccess(AVIMTypedMessage message) {
//      Utils.i();
      addMessageAndScroll(message);
    }
  }

  public void addMessageAndScroll(AVIMTypedMessage message) {
    AVIMTypedMessage foundMessage = findMessage(message.getMessageId());
    if (foundMessage == null) {
      adapter.add(message);
      scrollToLast();
    }
  }

  protected boolean filterException(Exception e) {
    if (e != null) {
      LogUtils.logException(e);
      showToast(e.getMessage());
      return false;
    } else {
      return true;
    }
  }


  protected void onAddLocationButtonClicked() {}

  protected void onLocationMessageViewClicked(AVIMLocationMessage locationMessage) {}

  protected void onImageMessageViewClicked(AVIMImageMessage imageMessage, String localImagePath) {}

  public void onEvent(MessageEvent messageEvent) {
    final AVIMTypedMessage message = messageEvent.getMessage();
    if (message.getConversationId().equals(conversation
      .getConversationId())) {
      if (messageEvent.getType() == MessageEvent.Type.Come) {
        new CacheMessagesTask(this, Arrays.asList(message)) {
          @Override
          void onPostRun(List<AVIMTypedMessage> messages, Exception e) {
            if (filterException(e)) {
              addMessageAndScroll(message);
            }
          }
        }.execute();
      } else if (messageEvent.getType() == MessageEvent.Type.Receipt) {
        //Utils.i("receipt");
        AVIMTypedMessage originMessage = findMessage(message.getMessageId());
        if (originMessage != null) {
          originMessage.setMessageStatus(message.getMessageStatus());
          originMessage.setReceiptTimestamp(message.getReceiptTimestamp());
          adapter.notifyDataSetChanged();
        }
      }
    }
  }

  public void onEvent(InputBottomBarEvent event) {
    switch (event.eventAction) {
      case InputBottomBarEvent.INPUTBOTTOMBAR_IMAGE_ACTION:
        selectImageFromLocal();
        break;
      case InputBottomBarEvent.INPUTBOTTOMBAR_CAMERA_ACTION:
        selectImageFromCamera();
        break;
      case InputBottomBarEvent.INPUTBOTTOMBAR_LOCATION_ACTION:
        onAddLocationButtonClicked();
        break;
    }
  }

  public void onEvent(InputBottomBarRecordEvent recordEvent) {
    messageAgent.sendAudio(recordEvent.audioPath);
  }

  public void onEvent(InputBottomBarTextEvent textEvent) {
    if (!TextUtils.isEmpty(textEvent.sendContent)) {
      messageAgent.sendText(textEvent.sendContent);
    }
  }
}
