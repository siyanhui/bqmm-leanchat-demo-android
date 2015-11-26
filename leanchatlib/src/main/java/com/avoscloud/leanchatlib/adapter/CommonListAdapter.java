package com.avoscloud.leanchatlib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import com.avoscloud.leanchatlib.viewholder.CommonViewHolder;
import com.avoscloud.leanchatlib.viewholder.CommonViewHolder.ViewHolderCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wli on 15/11/23.
 */
public class CommonListAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {

  private static HashMap<String, ViewHolderCreator> creatorHashMap = new HashMap<>();

  private Class<?> vhClass;

  protected List<T> dataList = new ArrayList<T>();

  public CommonListAdapter() {}

  public CommonListAdapter(Class<?> vhClass) {
    this.vhClass = vhClass;
  }

  public List<T> getDataList() {
    return dataList;
  }

  public void setDataList(List<T> datas) {
    dataList.clear();
    if (null != datas) {
      dataList.addAll(datas);
    }
  }

  public void addDataList(List<T> datas) {
    dataList.addAll(0, datas);
  }

  @Override
  public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (null == vhClass) {
      try {
        throw new IllegalArgumentException("please use CommonListAdapter(Class<VH> vhClass)");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    ViewHolderCreator<?> creator = null;
    if (creatorHashMap.containsKey(vhClass.getName())) {
      creator = creatorHashMap.get(vhClass.getName());
    } else {
      try {
        creator = (ViewHolderCreator)vhClass.getField("HOLDER_CREATOR").get(null);
        creatorHashMap.put(vhClass.getName(), creator);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
    if (null != creator) {
      return creator.createByViewGroupAndType(parent, viewType);
    } else {
      return null;
    }
  }

  @Override
  public void onBindViewHolder(CommonViewHolder holder, int position) {
    if (position >= 0 && position < dataList.size()) {
      holder.bindData(dataList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }
}