package com.github.jdsjlzx.interfaces;

import android.view.View;
import android.widget.TextView;

/**
 * RecyclerView的结果view接口，用于显示比如：列表获取数据失败、没有数据、点击加载数据等
 * 如果要自定义view，需要实现的该接口
 * 参照{@link com.github.jdsjlzx.view.ResultHeader}
 */
public interface IResultHeader {
    /**
     * 状态回调，回复初始设置
     */
    void onReset();

    /**
     * 状态回调，没有数据
     */
    void onNoData();

    /**
     * 状态回调，网络出错
     */
    void onNetWorkError();

    void onLoading();

    /**
     * ResultHeader
     */
    View getResultHeaderView();

    void setRefreshListener(OnRefreshListener onRefreshListener);

    void setResultTextView(TextView textView);

}
