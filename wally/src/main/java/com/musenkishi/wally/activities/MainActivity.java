/*
 * Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musenkishi.wally.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.musenkishi.wally.R;
import com.musenkishi.wally.adapters.SmartFragmentPagerAdapter;
import com.musenkishi.wally.base.BaseActivity;
import com.musenkishi.wally.base.BaseFragment;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.observers.FileReceiver;
import com.musenkishi.wally.observers.FiltersChangeReceiver;
import com.musenkishi.wally.views.TabBarView;

public class MainActivity extends BaseActivity {

    public static final String TAG = "com.musenkishi.wally.MainActivity";
    private static final String STATE_APPBAR_COLOR = TAG + ".AppBar.Color";

    private ViewPager viewPager;
    private SmartFragmentPagerAdapter pagerAdapter;
    private TabBarView tabBarView;
    private FileReceiver fileReceiver;
    private FiltersChangeReceiver filtersChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(12.0f);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.tab_bar);
            tabBarView = (TabBarView) getSupportActionBar().getCustomView();
        }

        fileReceiver = new FileReceiver();
        filtersChangeReceiver = new FiltersChangeReceiver();

        viewPager = (ViewPager) findViewById(R.id.fragment_pager);
        pagerAdapter = new SmartFragmentPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (tabBarView != null) {
                    tabBarView.setOffset(positionOffset);
                    tabBarView.setSelectedTab(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (pagerAdapter != null && pagerAdapter.getRegisteredFragment(position) != null) {
                    for (int i = 0; i < pagerAdapter.getCount(); i++) {
                        if (pagerAdapter.getRegisteredFragment(i) != null) {
                            pagerAdapter.getRegisteredFragment(i).setUserVisibleHint(position == i);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (tabBarView != null) {
            tabBarView.setSelectedTab(0);
        }

        tabBarView.setOnTabClickedListener(new TabBarView.OnTabClickedListener() {
            @Override
            public void onTabClicked(int index) {
                viewPager.setCurrentItem(index);
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_APPBAR_COLOR)){
            colorizeActionBar(savedInstanceState.getInt(STATE_APPBAR_COLOR));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (pagerAdapter != null && pagerAdapter.getRegisteredFragment(viewPager.getCurrentItem()) != null) {
            outState.putInt(STATE_APPBAR_COLOR, ((BaseFragment) pagerAdapter.getRegisteredFragment(viewPager.getCurrentItem())).getAppBarColor());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(fileReceiver, new IntentFilter(FileReceiver.GET_FILES));
        registerReceiver(filtersChangeReceiver, new IntentFilter(FiltersChangeReceiver.FILTERS_CHANGED));
    }

    @Override
    public void onPause() {
        unregisterReceiver(fileReceiver);
        unregisterReceiver(filtersChangeReceiver);
        super.onPause();
    }

    public void addOnFileChangedListener(FileReceiver.OnFileChangeListener onFileChangeListener) {
        if (fileReceiver != null) {
            fileReceiver.addListener(onFileChangeListener);
        }
    }

    public void addOnFiltersChangedListener(FiltersChangeReceiver.OnFiltersChangeListener onFiltersChangeListener) {
        if (filtersChangeReceiver != null) {
            filtersChangeReceiver.addListener(onFiltersChangeListener);
        }
    }

    @Override
    protected void handleReceivedIntent(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        if (WallyApplication.getDownloadIDs().containsKey(id)) {
            WallyApplication.getDownloadIDs().remove(id);
            if (fileReceiver != null) {
                Intent fileChangeIntent = new Intent(FileReceiver.GET_FILES);
                fileReceiver.onReceive(context, fileChangeIntent);
            }
            View heartTabImageView = tabBarView.getTab(4).getImageView();
            startHeartPopoutAnimation(heartTabImageView, Color.WHITE);
        }
    }

    public TabBarView getTabBarView() {
        return this.tabBarView;
    }

}
