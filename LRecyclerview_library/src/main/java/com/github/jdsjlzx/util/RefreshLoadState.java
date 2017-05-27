package com.github.jdsjlzx.util;

/**
 * 加载数据、下拉刷新、加载更多的状态，以后会替换原来的enum类
 */

public class RefreshLoadState {

    public static final int NORMAL = 0;         //正常
    public static final int NODATA = 1;         //没有数据
    public static final int NETWORKERROR = 2;   //没有网络/加载失败
    public static final int NOMORE = 3;         //没有更多数据了
    public static final int LOADING = 4;        //加载中

}
