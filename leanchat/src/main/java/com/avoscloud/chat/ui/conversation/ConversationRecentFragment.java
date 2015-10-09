package com.avoscloud.chat.ui.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMSingleMessageQueryCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.service.event.ConversationItemClickEvent;
import com.avoscloud.chat.ui.base_activity.BaseFragment;
import com.avoscloud.chat.ui.chat.ChatRoomActivity;
import com.avoscloud.chat.ui.view.ConversationListAdapter;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.event.MessageReceiptEvent;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.utils.AVUserCacheUtils;
import com.avoscloud.leanchatlib.utils.Constants;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ConversationRecentFragment extends BaseFragment implements ChatManager.ConnectionListener {

  @InjectView(R.id.im_client_state_view)
  View imClientStateView;

  @InjectView(R.id.fragment_conversation_srl_pullrefresh)
  protected SwipeRefreshLayout refreshLayout;

  @InjectView(R.id.fragment_conversation_srl_view)
  protected RecyclerView recyclerView;

  protected ConversationListAdapter<Room> itemAdapter;
  protected LinearLayoutManager layoutManager;

  private boolean hidden;
  private ConversationManager conversationManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.message_fragment, container, false);
    ButterKnife.inject(this, view);

    conversationManager = ConversationManager.getInstance();
    refreshLayout.setEnabled(false);
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    itemAdapter = new ConversationListAdapter<Room>();
    recyclerView.setAdapter(itemAdapter);
    EventBus.getDefault().register(this);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.conversation_messages);
    updateConversationList();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      updateConversationList();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      updateConversationList();
    }
  }

  public void onEvent(ConversationItemClickEvent event) {
    Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
    intent.putExtra(Constants.CONVERSATION_ID, event.conversationId);
    startActivity(intent);
  }

  public void onEvent(MessageReceiptEvent event) {
    List<Room> roomList = itemAdapter.getDataList();
    for(Room room : roomList) {
      if (room.getConversationId().equals(event.getMessage().getConversationId())) {
        itemAdapter.notifyItemChanged(roomList.indexOf(room));
        break;
      }
    }
  }

  private void updateConversationList() {
    conversationManager.findAndCacheRooms(new Room.MultiRoomsCallback() {
      @Override
      public void done(List<Room> rooms, AVException exception) {
        List<Room> sortedRooms = sortRooms(rooms);
        itemAdapter.setDataList(sortedRooms);
        itemAdapter.notifyDataSetChanged();

        updateLastMessage(sortedRooms);
        cacheRelatedUsers(sortedRooms);
      }
    });
  }

  private void updateLastMessage(final List<Room> roomList) {
    for (final Room room : roomList) {
      AVIMConversation conversation = ChatManager.getInstance().getConversation(room.getConversationId());
      room.setConversation(conversation);
      conversation.getLastMessage(new AVIMSingleMessageQueryCallback() {
        @Override
        public void done(AVIMMessage avimMessage, AVIMException e) {
          room.setLastMessage(avimMessage);
          int index = roomList.indexOf(room);
          itemAdapter.notifyItemChanged(index);
        }
      });
    }
  }

  private void cacheRelatedUsers(List<Room> rooms) {
    List<String> needCacheUsers = new ArrayList<String>();
    for(Room room : rooms) {
      AVIMConversation conversation = room.getConversation();
      if (ConversationHelper.typeOfConversation(conversation) == ConversationType.Single) {
        needCacheUsers.add(ConversationHelper.otherIdOfConversation(conversation));
      }
    }
    AVUserCacheUtils.cacheUsers(needCacheUsers, new AVUserCacheUtils.CacheUserCallback() {
      @Override
      public void done(Exception e) {
        itemAdapter.notifyDataSetChanged();
      }
    });
  }

  private List<Room> sortRooms(List<Room> roomList) {
    List<Room> sortedList = new ArrayList<Room>();
    sortedList.addAll(roomList);
    Collections.sort(sortedList, new Comparator<Room>() {
      @Override
      public int compare(Room lhs, Room rhs) {
        long leftTs = lhs.getLastMessageAt().getTime();
        long rightTs = rhs.getLastMessageAt().getTime();
        long value = leftTs - rightTs;
        if (value > 0) {
          return -1;
        } else if (value < 0) {
          return 1;
        } else {
          return 0;
        }
      }
    });
    return sortedList;
  }

  @Override
  public void onConnectionChanged(boolean connect) {
    imClientStateView.setVisibility(connect ? View.GONE : View.VISIBLE);
  }
}
