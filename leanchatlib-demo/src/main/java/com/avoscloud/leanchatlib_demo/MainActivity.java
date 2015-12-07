package com.avoscloud.leanchatlib_demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 主页面，包含三个 fragment，会话、联系人、我
 */
public class MainActivity extends AppCompatActivity {

  private Toolbar toolbar;
  private ViewPager viewPager;
  private TabLayout tabLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    viewPager = (ViewPager)findViewById(R.id.pager);
    tabLayout = (TabLayout)findViewById(R.id.tablayout);
    setTitle("Leanchat_demo");
    setSupportActionBar(toolbar);
    initTabLayout();
  }

  private void initTabLayout() {
    String[] tabList = new String[]{"会话", "联系人", "我"};
    final Fragment[] fragmentList = new Fragment[] {new ConversationFragment(),
      new ContactFragment(), new PersonalProfileFragment()};

    tabLayout.setTabMode(TabLayout.MODE_FIXED);
    for (int i = 0; i < tabList.length; i++) {
      tabLayout.addTab(tabLayout.newTab().setText(tabList[i]));
    }

    TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(),
      Arrays.asList(fragmentList), Arrays.asList(tabList));
    viewPager.setAdapter(adapter);
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageSelected(int position) {
        if (0 == position) {
          EventBus.getDefault().post(new ConversationFragmentUpdateEvent());
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {}
    });
    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setTabsFromPagerAdapter(adapter);
  }

  public class TabFragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments;
    private List<String> mTitles;

    public TabFragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
      super(fm);
      mFragments = fragments;
      mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mTitles.get(position);
    }
  }
}
