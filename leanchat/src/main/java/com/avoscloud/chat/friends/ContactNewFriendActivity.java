package com.avoscloud.chat.friends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.event.NewFriendItemClickEvent;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.event.ContactRefreshEvent;
import com.avoscloud.chat.viewholder.NewFriendItemHolder;
import com.avoscloud.leanchatlib.activity.AVBaseActivity;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.leanchatlib.model.LeanchatUser;
import com.avoscloud.leanchatlib.view.CustomRecyclerView;
import com.avoscloud.leanchatlib.view.LoadMoreFooterView;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ContactNewFriendActivity extends AVBaseActivity {

  @InjectView(R.id.newfriendList)
  CustomRecyclerView recyclerView;

  LinearLayoutManager layoutManager;

  private HeaderListAdapter<AddRequest> adapter;
  Handler handler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_new_friend_activity);
    ButterKnife.inject(this);
    initView();
    loadMoreAddRequest(true);
  }

  private void initView() {
    initActionBar(R.string.contact_new_friends);
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new HeaderListAdapter<>(NewFriendItemHolder.class);
    LoadMoreFooterView footerView = new LoadMoreFooterView(this);
    adapter.setFooterView(footerView);
    recyclerView.setOnLoadMoreStatusChangedListener(footerView);
    recyclerView.setAdapter(adapter);
    recyclerView.setOnLoadMoreListener(new CustomRecyclerView.OnLoadMoreListener() {
      @Override
      public void onLoadMore() {
        loadMoreAddRequest(false);
      }
    });
  }

    private void loadMoreAddRequest(final boolean isRefresh) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            List<AddRequest> addRequests = AddRequestManager.getInstance().findAddRequests(isRefresh ? 0 : adapter.getDataList().size(), 20);
            AddRequestManager.getInstance().markAddRequestsRead(addRequests);
            final List<AddRequest> filters = new ArrayList<AddRequest>();
            for (AddRequest addRequest : addRequests) {
              if (addRequest.getFromUser() != null) {
                filters.add(addRequest);
              }
            }
            PreferenceMap preferenceMap = new PreferenceMap(ContactNewFriendActivity.this, LeanchatUser.getCurrentUserId());
            preferenceMap.setAddRequestN(filters.size());
            handler.post(new Runnable() {
              @Override
              public void run() {
                recyclerView.setLoadComplete();
                if (isRefresh) {
                  adapter.setDataList(filters);
                } else {
                  adapter.addDataList(filters);
                }
                adapter.notifyDataSetChanged();
              }
            });

          } catch (AVException e) {
            e.printStackTrace();
          }
        }
      }).start();
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
            ConversationManager.getInstance().sendWelcomeMessage(addRequest.getFromUser().getObjectId());
          }
          loadMoreAddRequest(false);
          ContactRefreshEvent event = new ContactRefreshEvent();
          EventBus.getDefault().post(event);
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
