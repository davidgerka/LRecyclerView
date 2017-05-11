package com.github.jdsjlzx.interfaces;

import android.view.View;
import android.widget.TextView;

/**
 * RecyclerView的RefreshResultHeader需要实现的接口
 */
public interface IRefreshResult {
    /**
     * 状态回调，回复初始设置
     */
    void onReset();

    /**
     * 状态回调，加载中
     */
    void onNoData();

    /**
     * 状态回调，网络出错
     */
    void onNetWorkError();

    /**
     * RefreshResultHeader
     */
    View getResultHeaderView();

    void setRefreshListener(OnRefreshListener onRefreshListener);

    void setResultTextView(TextView textView);

}
