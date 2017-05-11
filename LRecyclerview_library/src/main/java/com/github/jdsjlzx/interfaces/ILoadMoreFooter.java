package com.github.jdsjlzx.interfaces;

import android.view.View;

/**
 * 加载更多FooterView需要实现的接口
 */
public interface ILoadMoreFooter {
    /**
     * 状态回调，回复初始设置
     */
    void onReset();

    /**
     * 状态回调，加载中
     */
    void onLoading();

    /**
     * 状态回调，加载完成
     */
    void onComplete();

    /**
     * 状态回调，已全部加载完成
     */
    void onNoMore();

    /**
     * 状态回调，网络出错
     */
    void onNetWorkError();

    /**
     * 加载更多的View
     */
    View getFootView();

    public enum State {
        Normal/**正常*/, NoMore/**加载到最底了*/, Loading/**加载中..*/, NetWorkError/**网络异常*/
    }
}
