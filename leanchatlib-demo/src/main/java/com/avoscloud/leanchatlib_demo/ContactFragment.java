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
import com.avoscloud.leanchatlib.activity.AVChatActivity;
import com.avoscloud.leanchatlib.adapter.CommonListAdapter;
import com.avoscloud.leanchatlib.utils.Constants;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/4.
 */
public class ContactFragment extends Fragment {

  @Bind(R.id.contact_fragment_srl_list)
  protected SwipeRefreshLayout refreshLayout;

  @Bind(R.id.contact_fragment_rv_list)
  protected RecyclerView recyclerView;

  private CommonListAdapter<ThirdPartUserUtils.ThirdPartUser> itemAdapter;
  LinearLayoutManager layoutManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.contact_fragment, container, false);
    ButterKnife.bind(this, view);

    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    itemAdapter = new CommonListAdapter(ContactItemHolder.class);
    recyclerView.setAdapter(itemAdapter);

    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        getMembers();
      }
    });
    return view;
  }

  @Override
  public void onPause() {
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    EventBus.getDefault().register(this);
    getMembers();
  }

  private void getMembers() {
    ThirdPartUserUtils.getInstance().getFriends(new ThirdPartUserUtils.FetchUserCallBack() {
      @Override
      public void done(List<ThirdPartUserUtils.ThirdPartUser> userList, Exception e) {
        refreshLayout.setRefreshing(false);
        itemAdapter.setDataList(userList);
        itemAdapter.notifyDataSetChanged();
      }
    });
  }

  public void onEvent(ContactItemClickEvent clickEvent) {
    Intent intent = new Intent(getActivity(), AVChatActivity.class);
    intent.putExtra(Constants.MEMBER_ID, clickEvent.memberId);
    startActivity(intent);
  }
}
