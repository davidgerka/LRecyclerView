package com.github.jdsjlzx.interfaces;

import android.view.View;
import android.widget.TextView;

/**
 * RecyclerView的下拉刷新结果view接口，如果用自定义view，需要实现的该接口
 * 参照{@link com.github.jdsjlzx.view.RefreshResultHeader}
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
