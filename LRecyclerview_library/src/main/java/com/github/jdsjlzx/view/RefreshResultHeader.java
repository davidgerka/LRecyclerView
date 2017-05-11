package com.github.jdsjlzx.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jdsjlzx.R;
import com.github.jdsjlzx.interfaces.IRefreshResult;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.util.RefreshLoadState;


/**
 * RecyclerView下拉刷新后，数据为空、刷新失败（网络异常等）等情况，可以用这个HeaderView来提示用户
 */
public class RefreshResultHeader extends RelativeLayout implements IRefreshResult {

    private TextView tvResult;
    private OnRefreshListener mOnRefreshListener;
    private int mStatus;

    private String loadingHint;
    private String noDataHint;
    private String noNetWorkHint;
    private int loadingColor = R.color.refreshresult;
    private int noDataColor = R.color.refreshresult;
    private int noNetWorkColor = R.color.refreshresult;

    public RefreshResultHeader(Context context, int resId) {
        super(context);
        init(context, resId);
    }

    public RefreshResultHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, -1);
    }

    public RefreshResultHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, -1);
    }

    public void init(Context context, int resId) {
        if (resId == -1) {
            inflate(context, R.layout.layout_recyclerview_refreshresultheader, this);
            setResultTextView((TextView) findViewById(R.id.tv_refreshresult));
        } else {
            inflate(context, resId, this);
        }

        initClick();
    }


    private void initClick() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
                setState(RefreshLoadState.LOADING);
            }
        });
    }

    @Override
    public void onReset() {
        setState(RefreshLoadState.NORMAL);
    }

    @Override
    public void onNoData() {
        setState(RefreshLoadState.NODATA);
    }

    @Override
    public void onNetWorkError() {
        setState(RefreshLoadState.NONETWORK);
    }

    @Override
    public View getResultHeaderView() {
        return this;
    }

    @Override
    public void setRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    @Override
    public void setResultTextView(TextView textView) {
        tvResult = textView;
    }


    /**
     * 设置状态
     *
     * @param status
     */
    public void setState(int status) {
        if (mStatus == status) {
            return;
        }
        mStatus = status;

        switch (status) {

            case RefreshLoadState.NORMAL:
                setClickable(false);
                this.setVisibility(GONE);
                break;
            case RefreshLoadState.LOADING:
                setClickable(false);
                this.setVisibility(VISIBLE);
                tvResult.setText(TextUtils.isEmpty(loadingHint) ? getResources().getString(R.string.refreshresult_loading) : loadingHint);
                tvResult.setTextColor(ContextCompat.getColor(getContext(), loadingColor));
                break;
            case RefreshLoadState.NODATA:
                setClickable(false);
                this.setVisibility(VISIBLE);
                tvResult.setText(TextUtils.isEmpty(noDataHint) ? getResources().getString(R.string.refreshresult_nodata) : noDataHint);
                tvResult.setTextColor(ContextCompat.getColor(getContext(), noDataColor));
                break;

            case RefreshLoadState.NONETWORK:
                setClickable(true);
                this.setVisibility(VISIBLE);
                tvResult.setText(TextUtils.isEmpty(noNetWorkHint) ? getResources().getString(R.string.refreshresult_nonetwork) : noNetWorkHint);
                tvResult.setTextColor(ContextCompat.getColor(getContext(), noNetWorkColor));
                break;
            default:
                break;
        }
    }
}