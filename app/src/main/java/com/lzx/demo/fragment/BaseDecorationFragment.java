package com.lzx.demo.fragment;

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

import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.lzx.demo.ItemDecoration.DividerDecoration;
import com.lzx.demo.R;
import com.lzx.demo.util.NetworkUtils;

import java.lang.ref.WeakReference;

public abstract class BaseDecorationFragment extends Fragment {

    private static final String TAG = BaseDecorationFragment.class.getSimpleName();

    private LRecyclerView mList;
    protected LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    private PreviewHandler mHandler = new PreviewHandler(this);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        mList = (LRecyclerView) view.findViewById(R.id.list);
        mList.setPullRefreshEnabled(true);
        mList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DividerDecoration divider = new DividerDecoration.Builder(this.getActivity())
                .setHeight(R.dimen.default_divider_height)
                .setPadding(R.dimen.default_divider_padding)
                .setColorResource(R.color.default_header_color)
                .build();

        mList.setHasFixedSize(true);
        mList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mList.addItemDecoration(divider);

        setAdapterAndDecor(mList);
    }

    protected abstract void setAdapterAndDecor(RecyclerView list);


    private class PreviewHandler extends Handler {

        private WeakReference<BaseDecorationFragment> ref;

        PreviewHandler(BaseDecorationFragment fragment) {
            ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final BaseDecorationFragment fragment = ref.get();
            if (fragment == null || fragment.isDetached()) {
                return;
            }
            switch (msg.what) {

                case -1:    //下拉刷新

                {
                    setAdapterAndDecor(fragment.mList);

                    fragment.mList.refreshComplete(0);
                }
                break;
                case -2:    //加载更多
//                    int currentSize = fragment.mDataAdapter.getItemCount();
//
//                    //模拟组装10个数据
//                    ArrayList<ItemModel> newList = new ArrayList<>();
//                    for (int i = 0; i < 10; i++) {
//                        if(currentSize + i > TOTAL_COUNTER){
//                            break;
//                        }
//
//                        ItemModel item = new ItemModel();
//                        item.id = currentSize + i;
//                        item.title = "item" + (item.id);
//
//                        newList.add(item);
//                    }
//
//                    fragment.addItems(newList);
//
//                    fragment.mRecyclerView.refreshComplete(REQUEST_COUNT);
                    break;
                case -3:
//                    fragment.mRecyclerView.refreshComplete(REQUEST_COUNT);
//                    fragment.notifyDataSetChanged();
//                    fragment.mRecyclerView.setOnNetWorkErrorListener(new OnNetWorkErrorListener() {
//                        @Override
//                        public void reload() {
//                            requestData();
//                        }
//                    });

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
                if(NetworkUtils.isNetAvailable(getContext().getApplicationContext())) {
                    mHandler.sendEmptyMessage(-1);
                } else {
                    mHandler.sendEmptyMessage(-3);
                }
            }
        }.start();
    }
}
