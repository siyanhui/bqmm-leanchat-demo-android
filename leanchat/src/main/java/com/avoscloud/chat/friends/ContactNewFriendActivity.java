package com.avoscloud.chat.friends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.event.ContactRefreshEvent;
import com.avoscloud.chat.event.NewFriendItemClickEvent;
import com.avoscloud.chat.model.LeanchatUser;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.viewholder.NewFriendItemHolder;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.view.RefreshableRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.greenrobot.event.EventBus;

public class ContactNewFriendActivity extends AVBaseActivity {

  @BindView(R.id.newfriendList)
  RefreshableRecyclerView recyclerView;

  LinearLayoutManager layoutManager;

  private HeaderListAdapter<AddRequest> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_new_friend_activity);
    initView();
    loadMoreAddRequest(true);
  }

  private void initView() {
    setTitle(R.string.contact_new_friends);
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new HeaderListAdapter<>(NewFriendItemHolder.class);
    recyclerView.setOnLoadDataListener(new RefreshableRecyclerView.OnLoadDataListener() {
      @Override
      public void onLoad(int skip, int limit, boolean isRefresh) {
        loadMoreAddRequest(false);
      }
    });
    recyclerView.setAdapter(adapter);
  }

    private void loadMoreAddRequest(final boolean isRefresh) {
      AddRequestManager.getInstance().findAddRequests(isRefresh ? 0 : adapter.getDataList().size(), 20, new FindCallback<AddRequest>() {
        @Override
        public void done(List<AddRequest> list, AVException e) {
          AddRequestManager.getInstance().markAddRequestsRead(list);
          final List<AddRequest> filters = new ArrayList<AddRequest>();
          for (AddRequest addRequest : list) {
            if (addRequest.getFromUser() != null) {
              filters.add(addRequest);
            }
          }
          PreferenceMap preferenceMap = new PreferenceMap(ContactNewFriendActivity.this, LeanchatUser.getCurrentUserId());
          preferenceMap.setAddRequestN(filters.size());
          recyclerView.setLoadComplete(list.toArray(), isRefresh);
        }
      });
    }

  public void onEvent(NewFriendItemClickEvent event) {
    if (event.isLongClick) {
      deleteAddRequest(event.addRequest);
    } else {
      agreeAddRequest(event.addRequest);
    }
  }

  private void agreeAddRequest(final AddRequest addRequest) {
    final ProgressDialog dialog = showSpinnerDialog();
    AddRequestManager.getInstance().agreeAddRequest(addRequest, new SaveCallback() {
      @Override
      public void done(AVException e) {
        dialog.dismiss();
        if (filterException(e)) {
          if (addRequest.getFromUser() != null) {
            sendWelcomeMessage(addRequest.getFromUser().getObjectId());
          }
          loadMoreAddRequest(false);
          ContactRefreshEvent event = new ContactRefreshEvent();
          EventBus.getDefault().post(event);
        }
      }
    });
  }

  public void sendWelcomeMessage(String toUserId) {
    ChatManager.getInstance().createSingleConversation(toUserId, new AVIMConversationCreatedCallback() {
      @Override
      public void done(AVIMConversation avimConversation, AVIMException e) {
        if (e == null) {
          AVIMTextMessage message = new AVIMTextMessage();
          message.setText(getString(R.string.message_when_agree_request));
          avimConversation.sendMessage(message, null);
        }
      }
    });
  }

  private void deleteAddRequest(final AddRequest addRequest) {
    new AlertDialog.Builder(this).setMessage(R.string.contact_deleteFriendRequest)
      .setPositiveButton(R.string.common_sure, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          addRequest.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
              loadMoreAddRequest(true);
            }
          });
        }
      }).setNegativeButton(R.string.chat_common_cancel, null).show();
  }
}
