package com.lzx.demo.multitype;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.lzx.demo.R;
import com.lzx.demo.adapter.ExpandableItemAdapter;
import com.lzx.demo.bean.ItemModel;
import com.lzx.demo.bean.Level0Item;
import com.lzx.demo.bean.Level1Item;
import com.lzx.demo.bean.MultiItemEntity;
import com.lzx.demo.bean.Person;
import com.lzx.demo.ui.EndlessLinearLayoutActivity;
import com.lzx.demo.util.NetworkUtils;
import com.lzx.demo.view.SampleHeader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * 带HeaderView、FooterView的LinearLayout RecyclerView
 */
public class ExpandableActivity extends AppCompatActivity {
    private static final String TAG = ExpandableActivity.class.getSimpleName();
    private LRecyclerView mRecyclerView = null;

    private ExpandableItemAdapter mDataAdapter = null;

    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private PreviewHandler mHandler = new PreviewHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_ll_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);


        mDataAdapter = new ExpandableItemAdapter(this);
        mDataAdapter.setDataList(generateData());

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        DividerDecoration divider = new DividerDecoration.Builder(this)
                .setHeight(R.dimen.default_divider_height)
                .setPadding(R.dimen.default_divider_padding)
                .setColorResource(R.color.split)
                .build();
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(divider);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //add a HeaderView
        View header = LayoutInflater.from(this).inflate(R.layout.sample_header,(ViewGroup)findViewById(android.R.id.content), false);

        mLRecyclerViewAdapter.addHeaderView(header);
        mLRecyclerViewAdapter.addHeaderView(new SampleHeader(this));

        //禁用下拉刷新功能
        mRecyclerView.setPullRefreshEnabled(true);
        mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(ExpandableActivity.this, "refresh", Toast.LENGTH_LONG).show();
                requestData();
            }
        });

        //禁用自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(true);

        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Toast.makeText(ExpandableActivity.this, "loadMore", Toast.LENGTH_LONG).show();
                requestLoadMoreData();
            }
        });

        View footerView = LayoutInflater.from(this).inflate(R.layout.sample_footer,(ViewGroup)findViewById(android.R.id.content), false);
        //add a FooterView
        mLRecyclerViewAdapter.addFooterView(footerView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private ArrayList<MultiItemEntity> generateData() {
        int lv0Count = 9;
        int lv1Count = 3;
        int personCount = 5;

        String[] nameList = {"Bob", "Andy", "Lily", "Brown", "Bruce"};
        Random random = new Random();

        ArrayList<MultiItemEntity> res = new ArrayList<>();
        for (int i = 0; i < lv0Count; i++) {
            Level0Item lv0 = new Level0Item("This is " + i + "th item in Level 0", "subtitle of " + i);
            for (int j = 0; j < lv1Count; j++) {
                Level1Item lv1 = new Level1Item("Level 1 item: " + j, "(no animation)");
                for (int k = 0; k < personCount; k++) {
                    lv1.addSubItem(new Person(nameList[k], random.nextInt(40)));
                }
                lv0.addSubItem(lv1);
            }
            res.add(lv0);
        }
        return res;
    }


    private class PreviewHandler extends Handler {

        private WeakReference<ExpandableActivity> ref;

        PreviewHandler(ExpandableActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ExpandableActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            Log.i(TAG, "---------------msg.what = "+msg.what);
            switch (msg.what) {

                case -1:    //下拉刷新

                {
                    mDataAdapter.setDataList(generateData());
                    activity.mRecyclerView.refreshComplete(10);
                }
                break;
                case -2:    //加载更多

                    mDataAdapter.addAll(generateData());
                    activity.mRecyclerView.refreshComplete(0);
                    break;
                case -3:
                    mDataAdapter.clear();
                    activity.mRecyclerView.refreshComplete(0);
                    mDataAdapter.notifyDataSetChanged();
                    activity.mRecyclerView.setOnNetWorkErrorListener(new OnNetWorkErrorListener() {
                        @Override
                        public void reload() {
                            requestData();
                        }
                    });

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 模拟请求网络
     */
    private void requestData() {
        Log.d(TAG, "requestData");
        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //模拟一下网络请求失败的情况
                if(NetworkUtils.isNetAvailable(getApplicationContext())) {
                    mHandler.sendEmptyMessage(-1);
                } else {
                    mHandler.sendEmptyMessage(-3);
                }
            }
        }.start();
    }

    /**
     * 模拟请求网络，加载更多
     */
    private void requestLoadMoreData() {
        Log.d(TAG, "requestLoadMoreData");
        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //模拟一下网络请求失败的情况
                if(NetworkUtils.isNetAvailable(getApplicationContext())) {
                    mHandler.sendEmptyMessage(-2);
                } else {
                    mHandler.sendEmptyMessage(-3);
                }
            }
        }.start();
    }


}