package com.lzx.demo;

import com.lzx.demo.base.BaseMainActivity;
import com.lzx.demo.multitype.MultiTypeActivity;
import com.lzx.demo.ui.CommonActivity;
import com.lzx.demo.ui.PulldownRefreshActivity;
import com.lzx.demo.ui.SectionCollectionActivity;
import com.lzx.demo.ui.SwipeMenuActivity;
import com.lzx.demo.ui.StickyGroupExpandActivity;

public class MainActivity extends BaseMainActivity {

    private static final Class<?>[] ACTIVITY = {CommonActivity.class, MultiTypeActivity.class, SectionCollectionActivity.class, SwipeMenuActivity.class,PulldownRefreshActivity.class, StickyGroupExpandActivity.class};
    private static final String[] TITLE = {"CommonActivity","MultiTypeActivity", "SectionCollectionActivity","SwipeMenuActivity","PulldownRefreshActivity", "StickyGroupExpandActivity"};


    @Override
    public Class<?>[] getActivitys() {
        return ACTIVITY;
    }

    @Override
    public String[] getTitles() {
        return TITLE;
    }
}
