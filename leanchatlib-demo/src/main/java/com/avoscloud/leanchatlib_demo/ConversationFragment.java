package com.avoscloud.leanchatlib_demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMSingleMessageQueryCallback;
import com.avoscloud.leanchatlib.activity.AVChatActivity;
import com.avoscloud.leanchatlib.adapter.CommonListAdapter;
import com.avoscloud.leanchatlib.event.ConversationItemClickEvent;
import com.avoscloud.leanchatlib.event.ImTypeMessageEvent;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.leanchatlib.utils.ConversationManager;
import com.avoscloud.leanchatlib.view.DividerItemDecoration;
import com.avoscloud.leanchatlib.viewholder.ConversationItemHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/4.
 */
public class ConversationFragment extends Fragment {
  @BindView(R.id.im_client_state_view)
  View imClientStateView;

  @BindView(R.id.fragment_conversation_srl_pullrefresh)
  protected SwipeRefreshLayout refreshLayout;

  @BindView(R.id.fragment_conversation_srl_view)
  protected RecyclerView recyclerView;

  protected CommonListAdapter<Room> itemAdapter;
  protected LinearLayoutManager layoutManager;

  private ConversationManager conversationManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.conversation_fragment, container, false);
    ButterKnife.bind(this, view);

    conversationManager = ConversationManager.getInstance();
    refreshLayout.setEnabled(false);
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
    itemAdapter = new CommonListAdapter<Room>(ConversationItemHolder.class);
    recyclerView.setAdapter(itemAdapter);
    EventBus.getDefault().register(this);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    updateConversationList();
  }

  @Override
  public void onResume() {
    super.onResume();
    updateConversationList();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
  }

  public void onEvent(ImTypeMessageEvent event) {
    updateConversationList();
  }

  private void updateConversationList() {
    conversationManager.findAndCacheRooms(new Room.MultiRoomsCallback() {
      @Override
      public void done(List<Room> roomList, AVException exception) {
        if (null == exception) {
          List<Room> sortedRooms = sortRooms(roomList);
          updateLastMessage(sortedRooms);
          itemAdapter.setDataList(sortedRooms);
          itemAdapter.notifyDataSetChanged();
        }
      }
    });
  }

  private void updateLastMessage(final List<Room> roomList) {
    for (final Room room : roomList) {
      AVIMConversation conversation = room.getConversation();
      if (null != conversation) {
        conversation.getLastMessage(new AVIMSingleMessageQueryCallback() {
          @Override
          public void done(AVIMMessage avimMessage, AVIMException e) {
            if (null != e && null != avimMessage) {
              room.setLastMessage(avimMessage);
              int index = roomList.indexOf(room);
              itemAdapter.notifyItemChanged(index);
            }
          }
        });
      }
    }
  }

  private List<Room> sortRooms(final List<Room> roomList) {
    List<Room> sortedList = new ArrayList<Room>();
    if (null != roomList) {
      sortedList.addAll(roomList);
      Collections.sort(sortedList, new Comparator<Room>() {
        @Override
        public int compare(Room lhs, Room rhs) {
          long value = lhs.getLastModifyTime() - rhs.getLastModifyTime();
          if (value > 0) {
            return -1;
          } else if (value < 0) {
            return 1;
          } else {
            return 0;
          }
        }
      });
    }
    return sortedList;
  }

  public void onEvent(ConversationItemClickEvent clickEvent) {
    Intent intent = new Intent(getContext(), AVChatActivity.class);
    intent.putExtra(Constants.CONVERSATION_ID, clickEvent.conversationId);
    getContext().startActivity(intent);
  }

  public void onEvent(ConversationFragmentUpdateEvent updateEvent) {
    updateConversationList();
  }
//
//  @Override
//  public void onConnectionChanged(boolean connect) {
//    imClientStateView.setVisibility(connect ? View.GONE : View.VISIBLE);
//  }
}
