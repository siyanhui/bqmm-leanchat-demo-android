package com.avoscloud.leanchatlib_demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.avoscloud.leanchatlib.adapter.HeaderListAdapter;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.avoscloud.leanchatlib.view.RefreshableRecyclerView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wli on 15/12/4.
 * 联系人页面
 */
public class ContactFragment extends Fragment {

  @Bind(R.id.contact_fragment_srl_list)
  protected SwipeRefreshLayout refreshLayout;

  @Bind(R.id.contact_fragment_rv_list)
  protected RefreshableRecyclerView recyclerView;

  private HeaderListAdapter<ThirdPartUserUtils.ThirdPartUser> itemAdapter;
  LinearLayoutManager layoutManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.contact_fragment, container, false);
    ButterKnife.bind(this, view);

    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setRelationSwipeLayout(refreshLayout);
    itemAdapter = new HeaderListAdapter<ThirdPartUserUtils.ThirdPartUser>(ContactItemHolder.class);
    recyclerView.setAdapter(itemAdapter);
    recyclerView.setRelationAdapter(itemAdapter);
    recyclerView.setOnLoadDataListener(new RefreshableRecyclerView.OnLoadDataListener() {
      @Override
      public void onLoad(int skip, int limit, boolean isRefresh) {
        getMembers(skip, limit, isRefresh);
      }
    });
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    recyclerView.refreshData();
  }

  private void getMembers(int skip, int limit, final boolean isRefresh) {
    ThirdPartUserUtils.getInstance().getFriends(skip, limit,
      new ThirdPartUserUtils.FetchUserCallBack() {
      @Override
      public void done(List<ThirdPartUserUtils.ThirdPartUser> userList, Exception e) {
        recyclerView.setLoadComplete(userList.toArray(), isRefresh);
      }
    });
  }
}
