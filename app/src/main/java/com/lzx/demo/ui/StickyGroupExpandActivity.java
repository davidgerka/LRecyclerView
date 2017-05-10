package com.lzx.demo.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzx.demo.R;
import com.lzx.demo.fragment.StickyGroupExpandFragment;

/**
 * 分组列表、并且GroupView会悬停在顶部
 */
public class StickyGroupExpandActivity extends AppCompatActivity  {

    private StickyGroupExpandFragment mCombatCommonFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout);
        initView();
    }

    private void initView() {
        mCombatCommonFragment = StickyGroupExpandFragment.newInstance();
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();  //开启一个事务
        fTransaction.replace(R.id.fl, mCombatCommonFragment);
        fTransaction.commit();
    }



    @Override
    public void onResume() {
        super.onResume();
    }


}
