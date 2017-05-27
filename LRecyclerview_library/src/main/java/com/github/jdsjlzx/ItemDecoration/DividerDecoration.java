package com.github.jdsjlzx.ItemDecoration;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;

/**
 * 普通列表分隔器，只适用于LRecyclerView，mDividerHeight为分隔器总高度，mLineHeight为分隔线高度。
 * 分隔线是画在分隔器的内顶部
 */
public class DividerDecoration extends RecyclerView.ItemDecoration {

    private int mDividerHeight; //分隔器总高度
    private int mLineHeight;    //分隔线高度
    private int mLPadding;
    private int mRPadding;
    private Paint mPaint;
    private int mBgColor;        //背景颜色
    private int mLineColor;      //分隔线颜色

    private DividerDecoration(int dividerHeight, int lineHeight, int lPadding, int rPadding, int bgColor, int lineColor) {
        mDividerHeight = dividerHeight;
        mLineHeight = lineHeight;
        mLPadding = lPadding;
        mRPadding = rPadding;
        mBgColor = bgColor;
        mLineColor = lineColor;
        mPaint = new Paint();
        mPaint.setColor(bgColor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        RecyclerView.Adapter adapter = parent.getAdapter();

        LRecyclerViewAdapter lRecyclerViewAdapter;
        if (adapter instanceof LRecyclerViewAdapter) {
            lRecyclerViewAdapter = (LRecyclerViewAdapter) adapter;
        } else {
            throw new RuntimeException("the adapter must be LRecyclerViewAdapter");
        }

        int count = parent.getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            final int top = child.getBottom();
            final int bottom = top + mDividerHeight;

            int left = child.getLeft() + mLPadding;
            int right = child.getRight() - mRPadding;

            int position = parent.getChildAdapterPosition(child);

            c.save();

            if (lRecyclerViewAdapter.isRefreshHeader(position) || lRecyclerViewAdapter.isHeader(position) || lRecyclerViewAdapter.isFooter(position)) {
                c.drawRect(0, 0, 0, 0, mPaint);
            } else {
                mPaint.setColor(mBgColor);
                c.drawRect(left, top, right, bottom, mPaint);
                mPaint.setColor(mLineColor);
                c.drawLine(left, top, right, top + mLineHeight, mPaint);
            }

            c.restore();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();

        LRecyclerViewAdapter lRecyclerViewAdapter;
        if (adapter instanceof LRecyclerViewAdapter) {
            lRecyclerViewAdapter = (LRecyclerViewAdapter) adapter;
        } else {
            throw new RuntimeException("the adapter must be LRecyclerViewAdapter");
        }

        int position = parent.getChildAdapterPosition(view);

        if (lRecyclerViewAdapter.isRefreshHeader(position) || lRecyclerViewAdapter.isHeader(position) || lRecyclerViewAdapter.isFooter(position)) {
            outRect.bottom = mDividerHeight;
            outRect.set(0, 0, 0, 0);
        } else {
            outRect.set(0, 0, 0, mDividerHeight);
        }

    }

    /**
     * A basic builder for divider decorations. The default builder creates a 1px thick black divider decoration.
     */
    public static class Builder {
        private Context mContext;
        private Resources mResources;
        private int mDividerHeight;
        private int mLineHeight;
        private int mLPadding;
        private int mRPadding;
        private int mBgColor;
        private int mLineColor;

        public Builder(Context context) {
            mContext = context;
            mResources = context.getResources();
            mDividerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 1f, context.getResources().getDisplayMetrics());
            mLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 1f, context.getResources().getDisplayMetrics());
            mLPadding = 0;
            mRPadding = 0;
            mBgColor = Color.TRANSPARENT;
            mLineColor = Color.GRAY;
        }

        /**
         * Set the divider height in pixels
         *
         * @param pixels height in pixels
         * @return the current instance of the Builder
         */
        public Builder setDividerHeight(float pixels) {
            mDividerHeight = (int) pixels;
            return this;
        }

        /**
         * Set the divider height in dp
         *
         * @param resource height resource id
         * @return the current instance of the Builder
         */
        public Builder setDividerHeight(@DimenRes int resource) {
            mDividerHeight = mResources.getDimensionPixelSize(resource);
            return this;
        }

        /**
         * Set the Line height in pixels
         *
         * @param pixels height in pixels
         * @return the current instance of the Builder
         */
        public Builder setLineHeight(float pixels) {
            mLineHeight = (int) pixels;
            return this;
        }

        /**
         * Set the Line height in dp
         *
         * @param resource height resource id
         * @return the current instance of the Builder
         */
        public Builder setLineHeight(@DimenRes int resource) {
            mLineHeight = mResources.getDimensionPixelSize(resource);
            return this;
        }

        /**
         * Sets both the left and right padding in pixels
         *
         * @param pixels padding in pixels
         * @return the current instance of the Builder
         */
        public Builder setPadding(float pixels) {
            setLeftPadding(pixels);
            setRightPadding(pixels);

            return this;
        }

        /**
         * Sets the left and right padding in dp
         *
         * @param resource padding resource id
         * @return the current instance of the Builder
         */
        public Builder setPadding(@DimenRes int resource) {
            setLeftPadding(resource);
            setRightPadding(resource);
            return this;
        }

        /**
         * Sets the left padding in pixels
         *
         * @param pixelPadding left padding in pixels
         * @return the current instance of the Builder
         */
        public Builder setLeftPadding(float pixelPadding) {
            mLPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixelPadding, mResources.getDisplayMetrics());

            return this;
        }

        /**
         * Sets the right padding in pixels
         *
         * @param pixelPadding right padding in pixels
         * @return the current instance of the Builder
         */
        public Builder setRightPadding(float pixelPadding) {
            mRPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixelPadding, mResources.getDisplayMetrics());

            return this;
        }

        /**
         * Sets the left padding in dp
         *
         * @param resource left padding resource id
         * @return the current instance of the Builder
         */
        public Builder setLeftPadding(@DimenRes int resource) {
            mLPadding = mResources.getDimensionPixelSize(resource);

            return this;
        }

        /**
         * Sets the right padding in dp
         *
         * @param resource right padding resource id
         * @return the current instance of the Builder
         */
        public Builder setRightPadding(@DimenRes int resource) {
            mRPadding = mResources.getDimensionPixelSize(resource);

            return this;
        }

        /**
         * Sets the divider bgColor
         *
         * @param resource the color attr id
         * @return
         */
        public Builder setBgColorAttr(@AttrRes int resource) {
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(resource, typedValue, true);
            setBgColorResource(typedValue.resourceId);
            return this;
        }

        /**
         * Sets the divider bgColor
         *
         * @param resource the color resource id
         * @return the current instance of the Builder
         */
        public Builder setBgColorResource(@ColorRes int resource) {
            setBgColor(ContextCompat.getColor(mContext, resource));
            return this;
        }

        /**
         * Sets the divider bgColor
         *
         * @param color the color
         * @return the current instance of the Builder
         */
        public Builder setBgColor(@ColorInt int color) {
            mBgColor = color;
            return this;
        }

        /**
         * Sets the lineColor
         *
         * @param resource the color attr id
         * @return
         */
        public Builder setLineColorAttr(@AttrRes int resource) {
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(resource, typedValue, true);
            setLineColorResource(typedValue.resourceId);
            return this;
        }

        /**
         * Sets the lineColor
         *
         * @param resource the color resource id
         * @return the current instance of the Builder
         */
        public Builder setLineColorResource(@ColorRes int resource) {
            setLineColor(ContextCompat.getColor(mContext, resource));
            return this;
        }

        /**
         * Sets the lineColor
         *
         * @param color the color
         * @return the current instance of the Builder
         */
        public Builder setLineColor(@ColorInt int color) {
            mLineColor = color;
            return this;
        }

        /**
         * Instantiates a DividerDecoration with the specified parameters.
         *
         * @return a properly initialized DividerDecoration instance
         */
        public DividerDecoration build() {
            return new DividerDecoration(mDividerHeight, mLineHeight, mLPadding, mRPadding, mBgColor, mLineColor);
        }
    }
}
