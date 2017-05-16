package com.github.jdsjlzx.recyclerview;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.github.jdsjlzx.interfaces.ILoadMoreFooter;
import com.github.jdsjlzx.interfaces.IRefreshHeader;
import com.github.jdsjlzx.interfaces.IRefreshResult;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.view.ArrowRefreshHeader;
import com.github.jdsjlzx.view.LoadingFooter;
import com.github.jdsjlzx.view.RefreshResultHeader;

/**
 * @author lizhixian
 * @created 2016/8/29 11:21
 */
public class LRecyclerView extends RecyclerView {
    private static final float DRAG_RATE = 2.0f;
    /**
     * 触发在上下滑动监听器的容差距离
     */
    private static final int HIDE_THRESHOLD = 20;
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();
    /**
     * 当前RecyclerView类型
     */
    protected LayoutManagerType layoutManagerType;
    private boolean mPullRefreshEnabled = true;
    private boolean mLoadMoreEnabled = true;
    private boolean mShowRefreshResult = true; //是否显示下拉刷新失败或者没有数据的view
    private boolean mRefreshing = false;//是否正在下拉刷新
    private boolean mLoadingData = false;//是否正在加载数据
    private OnRefreshListener mRefreshListener;
    private OnLoadMoreListener mLoadMoreListener;
    private OnNetWorkErrorListener mNetWorkErrorListener;
    private LScrollListener mLScrollListener;
    private IRefreshHeader mRefreshHeader;
    private ILoadMoreFooter mLoadMoreFooter;
    private IRefreshResult mRefreshResult;
    private View mEmptyView;
    private View mFootView; //用来显示：正在加载中、数据已全部加载、加载失败
    private View mRefreshResultView; //用来显示下拉刷新的结果：正在加载中、加载失败、没有数据
    private float mLastY = -1;
    private float sumOffSet;
    private int mPageSize = 10; //一次网络请求默认数量
    private LRecyclerViewAdapter mWrapAdapter;
    private boolean isNoMore = false;    //没有更多数据
    private boolean showNoMore = false;  //是否要显示"数据已全部加载"
    private boolean hasShowFooterView = false;   //是否已经显示了footerVeiw
    private boolean mIsVpDragger;
    private int mTouchSlop;
    private float startY;
    //scroll variables begin
    private float startX;
    private boolean isRegisterDataObserver;
    /**
     * 最后一个的位置
     */
    private int[] lastPositions;
    /**
     * 最后一个可见的item的位置
     */
    private int lastVisibleItemPosition;
    /**
     * 当前滑动的状态
     */
    private int currentScrollState = 0;
    /**
     * 滑动的距离
     */
    private int mDistance = 0;

    /**
     * 是否需要监听控制
     */
    private boolean mIsScrollDown = true;

    /**
     * Y轴移动的实际距离（最顶部为0）
     */
    private int mScrolledYDistance = 0;

    /**
     * X轴移动的实际距离（最左侧为0）
     */
    private int mScrolledXDistance = 0;
    //scroll variables end


    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    public LRecyclerView(Context context) {
        this(context, null);
    }

    public LRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext().getApplicationContext()).getScaledTouchSlop();
        if (mPullRefreshEnabled) {
            setRefreshHeader(new ArrowRefreshHeader(getContext().getApplicationContext()));
        }

        if (mLoadMoreEnabled) {
            setLoadMoreFooter(new LoadingFooter(getContext().getApplicationContext()));
        }

        setRefreshResultHeaderView(new RefreshResultHeader(getContext().getApplicationContext()));
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mWrapAdapter != null && mDataObserver != null && isRegisterDataObserver) {
            mWrapAdapter.getInnerAdapter().unregisterAdapterDataObserver(mDataObserver);
        }

        mWrapAdapter = (LRecyclerViewAdapter) adapter;
        super.setAdapter(mWrapAdapter);

        mWrapAdapter.getInnerAdapter().registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
        isRegisterDataObserver = true;

        mWrapAdapter.setRefreshHeader(mRefreshHeader);

        //fix bug: https://github.com/jdsjlzx/LRecyclerView/issues/115
        if (mLoadMoreEnabled && mWrapAdapter.getFooterViewsCount() == 0) {
            mWrapAdapter.addFooterView(mFootView);
            hasShowFooterView = true;
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mWrapAdapter != null && mDataObserver != null && isRegisterDataObserver) {
            mWrapAdapter.getInnerAdapter().unregisterAdapterDataObserver(mDataObserver);
            isRegisterDataObserver = false;
        }

    }

    /**
     * 解决嵌套RecyclerView滑动冲突问题
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY = ev.getY();
                startX = ev.getX();
                // 初始化标记
                mIsVpDragger = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果viewpager正在拖拽中，那么不拦截它的事件，直接return false；
                if (mIsVpDragger) {
                    return false;
                }

                // 获取当前手指位置
                float endY = ev.getY();
                float endX = ev.getX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
                if (distanceX > mTouchSlop && distanceX > distanceY) {
                    mIsVpDragger = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 初始化标记
                mIsVpDragger = false;
                break;
        }
        // 如果是Y轴位移大于X轴，事件交给swipeRefreshLayout处理。
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                sumOffSet = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = (ev.getRawY() - mLastY) / DRAG_RATE;
                mLastY = ev.getRawY();
                sumOffSet += deltaY;
                if (isOnTop() && mPullRefreshEnabled && !mRefreshing && (appbarState == AppBarStateChangeListener.State.EXPANDED)) {
                    mRefreshHeader.onMove(deltaY, sumOffSet);
                    if (mRefreshHeader.getVisibleHeight() > 0) {
                        if (null != mLScrollListener) {
                            mLScrollListener.onScrolled(mScrolledXDistance, (int) sumOffSet);    //在stickyView里有需要用到
                        }
                        return false;
                    }
                }

                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && mPullRefreshEnabled && !mRefreshing/*&& appbarState == AppBarStateChangeListener.State.EXPANDED*/) {
                    if (mRefreshHeader.onRelease()) {
                        if (mRefreshListener != null) {
                            mRefreshing = true;
                            mFootView.setVisibility(GONE);
                            mRefreshListener.onRefresh();

                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public boolean isOnTop() {
        return mPullRefreshEnabled && (mRefreshHeader.getHeaderView().getParent() != null);
    }

    /**
     * set view when no content item
     *
     * @param emptyView visiable view when items is empty
     */
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        mDataObserver.onChanged();
    }

    /**
     * @param pageSize 一页加载的数量
     */
    public void refreshComplete(int pageSize) {
        this.mPageSize = pageSize;
        if (mRefreshing) {
            isNoMore = false;
            mRefreshing = false;
            mRefreshHeader.refreshComplete();

            if (mWrapAdapter.getInnerAdapter().getItemCount() < pageSize) {
                mFootView.setVisibility(GONE);
                setShowFooterView(false);
            } else {
                setShowFooterView(true);
            }
        } else if (mLoadingData) {
            mLoadingData = false;
            mLoadMoreFooter.onComplete();
        }
    }

    /**
     * 下拉刷新/上拉加载完成之后都要调用该方法
     *
     * @param pageSize 每一页的数据为多少项，不知道的话填0
     * @param noMore   该参数只对上拉加载更多有效，没有更多分页了就填true
     */
    public void refreshComplete(int pageSize, boolean noMore) {
        this.mPageSize = pageSize;
        if (mRefreshing) {
            mRefreshing = false;
            mRefreshHeader.refreshComplete();
            int itemCount = mWrapAdapter.getInnerAdapter().getItemCount();
            if(itemCount == 0){     //没有数据
                setNoMore(true);
                setShowFooterView(false);
            }else if(itemCount < pageSize) {    //item不够一页的数目
                setNoMore(true);
                setShowFooterView(showNoMore);
            }else {
                setNoMore(false);
                setShowFooterView(true);
            }
        } else if (mLoadingData) {
            setNoMore(noMore);
            if (noMore) { //没有更多数据
                setShowFooterView(showNoMore);
            } else { //还有数据
                setShowFooterView(true);
            }
        }
    }

    /**
     * 下拉刷新/上拉加载完成之后都要调用该方法
     *
     * @param pageSize 每一页的数据为多少项，不知道的话填0
     * @param noMore   该参数只对上拉加载更多有效，没有更多分页了就填true
     */
    public void refreshComplete(int pageSize, boolean noMore, boolean suc) {
        this.mPageSize = pageSize;
        if (mRefreshing) {
            mRefreshing = false;
            mRefreshHeader.refreshComplete();
            int itemCount = mWrapAdapter.getInnerAdapter().getItemCount();
            if(itemCount == 0){     //没有数据
                setNoMore(true);
                setShowFooterView(false);
            }else if(itemCount < pageSize) {    //item不够一页的数目
                setNoMore(true);
                setShowFooterView(showNoMore);
            }else {
                setNoMore(false);
                setShowFooterView(true);
            }
        } else if (mLoadingData) {
            setNoMore(noMore);
            if (noMore) { //没有更多数据
                setShowFooterView(showNoMore);
            } else { //还有数据
                setShowFooterView(true);
            }
        }
    }


    /**
     * 设置是否要显示"数据已全部加载"view
     *
     * @param show
     */
    public void setShowNoMore(boolean show) {
        showNoMore = show;
    }

    /**
     * 设置是否已加载全部
     *
     * @param noMore
     */
    public void setNoMore(boolean noMore) {
        mLoadingData = false;
        isNoMore = noMore;
        if (isNoMore) {
            mLoadMoreFooter.onNoMore();
        } else {
            mLoadMoreFooter.onComplete();
        }
    }

    /**
     * 是否显示底部mFootView
     *
     * @param show
     */
    private void setShowFooterView(boolean show) {
        if (!mLoadMoreEnabled || mWrapAdapter == null) {
            return;
        }
        if (show) {
            mFootView.setVisibility(VISIBLE);
            if (hasShowFooterView) {
                return;
            }
            hasShowFooterView = true;
            mWrapAdapter.addFooterView(mFootView);
        } else {
            mFootView.setVisibility(GONE);
            if (!hasShowFooterView) {
                return;
            }
            hasShowFooterView = false;
            mWrapAdapter.removeFooterView();
        }
    }

    /**
     * 设置自定义的RefreshHeader
     * 注意：setRefreshHeader方法必须在setAdapter方法之前调用才能生效
     */
    public void setRefreshHeader(IRefreshHeader refreshHeader) {
        if (isRegisterDataObserver) {
            throw new RuntimeException("setRefreshHeader must been invoked before setting the adapter.");
        }
        this.mRefreshHeader = refreshHeader;
    }

    /**
     * 设置自定义的footerview
     */
    public void setLoadMoreFooter(ILoadMoreFooter loadMoreFooter) {
        this.mLoadMoreFooter = loadMoreFooter;
        mFootView = loadMoreFooter.getFootView();
        mFootView.setVisibility(GONE);

        //wxm:mFootView inflate的时候没有以RecyclerView为parent，所以要设置LayoutParams
        ViewGroup.LayoutParams layoutParams = mFootView.getLayoutParams();
        if (layoutParams != null) {
            mFootView.setLayoutParams(new LayoutParams(layoutParams));
        } else {
            mFootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * 设置自定义的footerview
     */
    public void setRefreshResultHeaderView(IRefreshResult refreshResult) {
        this.mRefreshResult = refreshResult;
        mRefreshResultView = refreshResult.getResultHeaderView();
        mRefreshResultView.setVisibility(GONE);

        //wxm:mFootView inflate的时候没有以RecyclerView为parent，所以要设置LayoutParams
        ViewGroup.LayoutParams layoutParams = mRefreshResultView.getLayoutParams();
        if (layoutParams != null) {
            mRefreshResultView.setLayoutParams(new LayoutParams(layoutParams));
        } else {
            mRefreshResultView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    public void setPullRefreshEnabled(boolean enabled) {
        mPullRefreshEnabled = enabled;
    }

    /**
     * 到底加载是否可用
     */
    public void setLoadMoreEnabled(boolean enabled) {
        if (mWrapAdapter == null) {
            throw new NullPointerException("LRecyclerViewAdapter cannot be null, please make sure the variable mWrapAdapter have been initialized.");
        }
        mLoadMoreEnabled = enabled;
        if (!enabled) {
            if (null != mWrapAdapter) {
                mWrapAdapter.removeFooterView();
                hasShowFooterView = false;
            } else {
                mLoadMoreFooter.onReset();
            }
        }
    }

    public void setRefreshProgressStyle(int style) {
        if (mRefreshHeader != null && mRefreshHeader instanceof ArrowRefreshHeader) {
            ((ArrowRefreshHeader) mRefreshHeader).setProgressStyle(style);
        }
    }

    public void setArrowImageView(int resId) {
        if (mRefreshHeader != null && mRefreshHeader instanceof ArrowRefreshHeader) {
            ((ArrowRefreshHeader) mRefreshHeader).setArrowImageView(resId);
        }
    }

    /**
     * 设置LoadMoreFooter加载中的进度条样式，只对默认的LoadingFooter有效，如果是自己定义的LoadMoreFooter，自己在view里面实现
     * @param style
     */
    public void setLoadingMoreProgressStyle(int style) {
        if (mLoadMoreFooter != null && mLoadMoreFooter instanceof LoadingFooter) {
            ((LoadingFooter) mLoadMoreFooter).setProgressStyle(style);
        }

    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public void setOnNetWorkErrorListener(final OnNetWorkErrorListener listener) {
        mNetWorkErrorListener = listener;
        if(mLoadMoreFooter != null && mFootView != null){
            mLoadMoreFooter.onNetWorkError();
            mFootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLoadMoreFooter.onLoading();
                    mNetWorkErrorListener.reload();
                }
            });
        }
    }

    /**
     * 设置LoadMoreFooter各个状态的提示语，只对默认的LoadingFooter有效，如果是自己定义的LoadMoreFooter，自己在view里面实现
     * @param loading
     * @param noMore
     * @param noNetWork
     */
    public void setFooterViewHint(String loading, String noMore, String noNetWork) {
        if (mLoadMoreFooter != null && mLoadMoreFooter instanceof LoadingFooter) {
            LoadingFooter loadingFooter = ((LoadingFooter) mLoadMoreFooter);
            loadingFooter.setLoadingHint(loading);
            loadingFooter.setNoMoreHint(noMore);
            loadingFooter.setNoNetWorkHint(noNetWork);
        }
    }

    /**
     * 设置LoadMoreFooter文字颜色，只对默认的LoadingFooter有效，如果是自己定义的LoadMoreFooter，自己在view里面实现
     *
     * @param indicatorColor
     * @param hintColor
     * @param backgroundColor
     */
    public void setFooterViewColor(int indicatorColor, int hintColor, int backgroundColor) {
        if (mLoadMoreFooter != null && mLoadMoreFooter instanceof LoadingFooter) {
            LoadingFooter loadingFooter = ((LoadingFooter) mLoadMoreFooter);
            loadingFooter.setIndicatorColor(ContextCompat.getColor(getContext(), indicatorColor));
            loadingFooter.setHintTextColor(hintColor);
            loadingFooter.setViewBackgroundColor(backgroundColor);
        }
    }

    /**
     * 设置颜色
     *
     * @param indicatorColor  Only call the method setRefreshProgressStyle(int style) to take effect
     * @param hintColor
     * @param backgroundColor
     */
    public void setHeaderViewColor(int indicatorColor, int hintColor, int backgroundColor) {
        if (mRefreshHeader != null && mRefreshHeader instanceof ArrowRefreshHeader) {
            ArrowRefreshHeader arrowRefreshHeader = ((ArrowRefreshHeader) mRefreshHeader);
            arrowRefreshHeader.setIndicatorColor(ContextCompat.getColor(getContext(), indicatorColor));
            arrowRefreshHeader.setHintTextColor(hintColor);
            arrowRefreshHeader.setViewBackgroundColor(backgroundColor);
        }

    }

    public void setLScrollListener(LScrollListener listener) {
        mLScrollListener = listener;
    }

    public void refresh() {
        if (mRefreshHeader.getVisibleHeight() > 0 || mRefreshing) {// if RefreshHeader is Refreshing, return
            return;
        }
        if (mPullRefreshEnabled && mRefreshListener != null) {
            mRefreshHeader.onRefreshing();
            int offSet = mRefreshHeader.getHeaderView().getMeasuredHeight();
            mRefreshHeader.onMove(offSet, offSet);
            mRefreshing = true;

            mFootView.setVisibility(GONE);
            mRefreshListener.onRefresh();
        }
    }

    public void forceToRefresh() {
        if (mLoadingData) {
            return;
        }
        refresh();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        int firstVisibleItemPosition = 0;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();

        if (layoutManagerType == null) {
            if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LayoutManagerType.LinearLayout;
            } else if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LayoutManagerType.GridLayout;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LayoutManagerType.StaggeredGridLayout;
            } else {
                throw new RuntimeException(
                        "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }

        switch (layoutManagerType) {
            case LinearLayout:
                firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case GridLayout:
                firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case StaggeredGridLayout:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(lastPositions);
                firstVisibleItemPosition = findMax(lastPositions);
                break;
        }

        // 根据类型来计算出第一个可见的item的位置，由此判断是否触发到底部的监听器
        // 计算并判断当前是向上滑动还是向下滑动
        calculateScrollUpOrDown(firstVisibleItemPosition, dy);
        // 移动距离超过一定的范围，我们监听就没有啥实际的意义了
        mScrolledXDistance += dx;
        mScrolledYDistance += dy;
        mScrolledXDistance = (mScrolledXDistance < 0) ? 0 : mScrolledXDistance;
        mScrolledYDistance = (mScrolledYDistance < 0) ? 0 : mScrolledYDistance;
        if (mIsScrollDown && (dy == 0)) {
            mScrolledYDistance = 0;
        }
        //Be careful in here
        if (null != mLScrollListener) {
            mLScrollListener.onScrolled(mScrolledXDistance, mScrolledYDistance);
        }

    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        currentScrollState = state;

        if (mLScrollListener != null) {
            mLScrollListener.onScrollStateChanged(state);
        }

        if (mLoadMoreListener != null && mLoadMoreEnabled) {
            if (currentScrollState == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if (visibleItemCount > 0
                        && lastVisibleItemPosition >= totalItemCount - 1
                        && totalItemCount > visibleItemCount
                        && !isNoMore
                        && !mRefreshing) {

                    mFootView.setVisibility(View.VISIBLE);
                    if (mLoadingData) {
                        return;
                    } else {
                        mLoadingData = true;
                        mLoadMoreFooter.onLoading();
                        mLoadMoreListener.onLoadMore();
                    }

                }

            }
        }

    }

    /**
     * 计算当前是向上滑动还是向下滑动
     */
    private void calculateScrollUpOrDown(int firstVisibleItemPosition, int dy) {
        if (null != mLScrollListener) {
            if (firstVisibleItemPosition == 0) {
                if (!mIsScrollDown) {
                    mIsScrollDown = true;
                    mLScrollListener.onScrollDown();
                }
            } else {
                if (mDistance > HIDE_THRESHOLD && mIsScrollDown) {
                    mIsScrollDown = false;
                    mLScrollListener.onScrollUp();
                    mDistance = 0;
                } else if (mDistance < -HIDE_THRESHOLD && !mIsScrollDown) {
                    mIsScrollDown = true;
                    mLScrollListener.onScrollDown();
                    mDistance = 0;
                }
            }
        }

        if ((mIsScrollDown && dy > 0) || (!mIsScrollDown && dy < 0)) {
            mDistance += dy;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //解决LRecyclerView与CollapsingToolbarLayout滑动冲突的问题
        AppBarLayout appBarLayout = null;
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if (p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if (child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout) child;
                    break;
                }
            }
            if (appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }
    }

    public enum LayoutManagerType {
        LinearLayout,
        StaggeredGridLayout,
        GridLayout
    }

    public interface LScrollListener {

        void onScrollUp();//scroll down to up

        void onScrollDown();//scroll from up to down

        void onScrolled(int distanceX, int distanceY);// moving state,you can get the move distance

        void onScrollStateChanged(int state);
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter instanceof LRecyclerViewAdapter) {
                LRecyclerViewAdapter lRecyclerViewAdapter = (LRecyclerViewAdapter) adapter;
                if (lRecyclerViewAdapter.getInnerAdapter() != null && mEmptyView != null) {
                    int count = lRecyclerViewAdapter.getInnerAdapter().getItemCount();
                    if (count == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                        LRecyclerView.this.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);
                        LRecyclerView.this.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (adapter != null && mEmptyView != null) {
                    if (adapter.getItemCount() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                        LRecyclerView.this.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);
                        LRecyclerView.this.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
//                if(mWrapAdapter.getInnerAdapter().getItemCount() < mPageSize ) {
//                    mFootView.setVisibility(GONE);
//                }
            }


        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart + mWrapAdapter.getHeaderViewsCount() + 1, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart + mWrapAdapter.getHeaderViewsCount() + 1, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart + mWrapAdapter.getHeaderViewsCount() + 1, itemCount);
//            if(mWrapAdapter.getInnerAdapter().getItemCount() < mPageSize ) {
//                mFootView.setVisibility(GONE);
//            }

            if (mWrapAdapter.getInnerAdapter().getItemCount() == 0) {     //少于1页的数据
                setNoMore(true);
                setShowFooterView(false);
            }else if (mWrapAdapter.getInnerAdapter().getItemCount() < mPageSize) {     //少于1页的数据
                setNoMore(true);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int headerViewsCountCount = mWrapAdapter.getHeaderViewsCount();
            mWrapAdapter.notifyItemRangeChanged(fromPosition + headerViewsCountCount + 1, toPosition + headerViewsCountCount + 1 + itemCount);
        }

    }

}
