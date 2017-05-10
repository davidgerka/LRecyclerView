package com.lzx.demo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.handmark.pulltorefresh.library.BaseGroupInfo;
import com.lzx.demo.R;
import com.lzx.demo.adapter.ExpandableRecyclerAdapter;
import com.lzx.demo.adapter.StickyGroupExpandAdapter;
import com.lzx.demo.bean.StickyGroupExpandDataManager;

import java.lang.ref.WeakReference;

/**
 * 分组列表、并且GroupView会悬停在顶部的Fragment
 * Created by lqn on 17/5/5.
 */

public class StickyGroupExpandFragment extends Fragment {
    private static final String TAG = StickyGroupExpandFragment.class.getSimpleName();
    private static final String REFRESH = TAG + "_REFRESH";
    private static final String LOADMORE = TAG + "_LOADMORE";
    private static final int PAGE_SIZE = 20;    //一页加载20条
    private StickyGroupExpandFragment _this;
    private LRecyclerView mLRecyclerView;
    private StickyGroupExpandAdapter mDataAdapter = null;
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;
    private LinearLayoutManager mLinearLayoutManager;
    private int lvCurrentPage = 1, lvTotalPage = 0;
    private boolean isRefresh = false;
    private View clickView;
    private StickyGroupExpandDataManager dataManager = new StickyGroupExpandDataManager();
    private View userInfoHeaderView;
    private View resultHeaderView;

    private View stickyView;
    private int stickyHeight;
    private int mCurrentPosition = 0;
    private TextView tvGroup;
    private ImageView ivArrow;

    private StickyGroupExpandFragment.PreviewHandler mHandler = new StickyGroupExpandFragment.PreviewHandler(this);

    public static StickyGroupExpandFragment newInstance() {
        StickyGroupExpandFragment fragment = new StickyGroupExpandFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        _this = this;
        View view = inflater.inflate(R.layout.fragment_combat_record, container, false);
        findViewById(view);
        init();
        return view;
    }

    private void findViewById(View view) {
        mLRecyclerView = (LRecyclerView) view.findViewById(R.id.recyclerView);
        stickyView = view.findViewById(R.id.stickyview);
        tvGroup = (TextView) stickyView.findViewById(R.id.tv_group);
        ivArrow = (ImageView) stickyView.findViewById(R.id.iv_arrow);
    }

    private void init() {
        mDataAdapter = new StickyGroupExpandAdapter(getActivity(), mLRecyclerView);
        mDataAdapter.setMode(ExpandableRecyclerAdapter.MODE_NORMAL);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mLRecyclerView.setAdapter(mLRecyclerViewAdapter);
        mLRecyclerView.setShowNoMore(true);

        if (userInfoHeaderView != null) {
            mLRecyclerViewAdapter.addHeaderView(userInfoHeaderView);
        }
        if (resultHeaderView != null) {
            mLRecyclerViewAdapter.addHeaderView(resultHeaderView);
        }
        if (mLRecyclerViewAdapter.getHeaderViewsCount() == 0) {
            View view = new View(getActivity());
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50));
            view.setBackgroundColor(Color.GREEN);
            mLRecyclerViewAdapter.addHeaderView(view);   //添加HeaderView，避免开源库中计算position出错导致数组溢出的bug
        }

        mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLRecyclerView.setLayoutManager(mLinearLayoutManager);

        mLRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mLRecyclerView.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);

        mLRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        //设置底部加载文字提示
//        mLRecyclerView.setFooterViewHint(getString(R.string.recyclerview_loadmore_ing), getString(R.string.recyclerview_loadmore_nomore), getString(R.string.recyclerview_loadmore_tryagain));

        mLRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                refresh();
            }
        });

        mLRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (lvCurrentPage >= lvTotalPage) {
//                    mLRecyclerView.setNoMore(true);
                    mLRecyclerView.refreshComplete(1, true);
                } else {
                    loadMore();
                }
            }
        });

        mLRecyclerView.setLScrollListener(new LRecyclerView.LScrollListener() {

            @Override
            public void onScrollUp() {
            }

            @Override
            public void onScrollDown() {
            }


            @Override
            public void onScrolled(int distanceX, int distanceY) {

                if (isClickGroupView || mCurrentPosition != mLinearLayoutManager.findFirstVisibleItemPosition()) {  //判断是否换了第一个显示的item
                    isClickGroupView = false;
                    mCurrentPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                    stickyView.setY(0);
                    updateStickyView(); //这里更新StickView时，一定要判断当前第一个item是输入哪个组的，然后获取当前组的数据来更新StickView
                }
                //我们只是简单的收窄了我们让悬浮条移动的条件，这里就是ItemType必须对应时才发生移动
                int nextType = mLRecyclerViewAdapter.getItemViewType(mCurrentPosition + 1);
                int nowType = mLRecyclerViewAdapter.getItemViewType(mCurrentPosition);
                if (nowType == ExpandableRecyclerAdapter.TYPE_HEADER || nowType == ExpandableRecyclerAdapter.TYPE_CONTENT) {
                    stickyView.setVisibility(View.VISIBLE);
                } else {
                    stickyView.setVisibility(View.INVISIBLE);
                }

                //如果下一个item是组，就进行组的移动操作
                if (nextType == ExpandableRecyclerAdapter.TYPE_HEADER) {
                    View view = mLinearLayoutManager.findViewByPosition(mCurrentPosition + 1);
                    if (view != null) {
                        if (view.getTop() <= stickyHeight) {
                            stickyView.setY(-(stickyHeight - view.getTop()));
                        } else {
                            stickyView.setY(0);
                        }
                    }
                }


            }

            @Override
            public void onScrollStateChanged(int state) {
                stickyHeight = stickyView.getHeight();
            }

        });


        mLRecyclerView.refresh();
    }

    private void updateStickyView() {
        int nowType = mLRecyclerViewAdapter.getItemViewType(mCurrentPosition);
        if (nowType == ExpandableRecyclerAdapter.TYPE_HEADER || nowType == ExpandableRecyclerAdapter.TYPE_CONTENT) {
            stickyView.setVisibility(View.VISIBLE);

            int headerViewCount = mLRecyclerViewAdapter.getHeaderViewsCount();
            final int position = mCurrentPosition - (headerViewCount + 1);

            final int index = mDataAdapter.getIndexWithPosition(position);
            if (index == -1) {
                return;
            }

            BaseGroupInfo baseGroupInfo = mDataAdapter.getGroupModelWithIndex(index);

            if (baseGroupInfo != null) {
                tvGroup.setText(baseGroupInfo.groupTitle);
                final boolean isExpanded = mDataAdapter.isExpanded(index);
                if (isExpanded) {
                    ivArrow.setRotation(180);
                } else {
                    ivArrow.setRotation(90);
                }


                stickyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isClickGroupView = true;
                        if (mDataAdapter.toggleExpandedItems(index, true)) {
                            openArrow(ivArrow);
                        } else {
                            closeArrow(ivArrow);
                        }
                    }
                });
            }
        } else {
            stickyView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isClickGroupView = false;
    private static final int ARROW_ROTATION_DURATION = 150;
    public static void openArrow(View view) {
        view.setRotation(180);
//        view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(180);
    }

    public static void closeArrow(View view) {
        view.setRotation(90);
//        view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(90);
    }

    /**
     * 下拉刷新获取数据
     */
    private void refresh() {
        requestData();
    }

    /**
     * 上拉加载更多数据
     */
    private void loadMore() {
    }

    /**
     * 设置头部视图
     *
     * @param view
     */
    public void setUserInfoHeaderView(View view) {
        userInfoHeaderView = view;
    }

    /**
     * 设置头部视图
     *
     * @param view
     */
    public void setResultHeaderView(View view) {
        resultHeaderView = view;
    }


    /**
     * 模拟请求网络
     */
    private void requestData() {
        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(-1);

            }
        }.start();
    }

    private class PreviewHandler extends Handler {

        private WeakReference<StickyGroupExpandFragment> ref;

        PreviewHandler(StickyGroupExpandFragment fragment) {
            ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final StickyGroupExpandFragment fragment = ref.get();
            if (fragment == null || fragment.isDetached()) {
                return;
            }
            switch (msg.what) {

                case -1:    //下拉刷新

                {
                    mCurrentPosition = 0;
                    dataManager.updateRecordList("");
                    mDataAdapter.setGroupItems(dataManager.dataList);
                    mLRecyclerView.refreshComplete(1, true);
                    updateStickyView();
                }
                break;
                case -2:    //加载更多

                    break;
                case -3:


                    break;
                default:
                    break;
            }
        }
    }

}
