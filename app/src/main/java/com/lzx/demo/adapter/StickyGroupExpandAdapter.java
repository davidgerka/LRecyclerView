package com.lzx.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lzx.demo.bean.ContentInfo;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.handmark.pulltorefresh.library.BaseGroupInfo;
import com.handmark.pulltorefresh.library.BaseModelInfo;
import com.lzx.demo.R;

/**
 * StickyGroupView的适配器，分组显示，可展开/收起
 */
public class StickyGroupExpandAdapter extends ExpandableRecyclerAdapter<BaseModelInfo> {
    private LRecyclerView recyclerView;

    public StickyGroupExpandAdapter(Context context, LRecyclerView recyclerView) {
        super(context);
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                //header中的箭头默认隐藏，如有需要，item_arrow设置为visible即可
                return new ItemHeaderViewHolder(inflate(R.layout.item_group, parent), recyclerView);
            case TYPE_CONTENT:
            default:
                return new ItemChildViewHolder(inflate(R.layout.item_content, parent));
        }
    }

    @Override
    public void onBindViewHolder(ExpandableRecyclerAdapter.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                ((ItemHeaderViewHolder) holder).bind(position);
                break;
            case TYPE_CONTENT:
            default:
                ((ItemChildViewHolder) holder).bind(position);
                break;
        }


    }

    public class ItemHeaderViewHolder extends ExpandableRecyclerAdapter.HeaderViewHolder {
        View vHeader;
        TextView tvGroup;


        public ItemHeaderViewHolder(View view, LRecyclerView recyclerView) {
            super(view, (ImageView) view.findViewById(R.id.iv_arrow), recyclerView);
            tvGroup = (TextView) view.findViewById(R.id.tv_group);
        }

        public void bind(int position) {
            super.bind(position);
            BaseModelInfo baseModelInfo = visibleItems.get(position);
            if (baseModelInfo != null && baseModelInfo instanceof BaseGroupInfo) {
                BaseGroupInfo baseGroupInfo = (BaseGroupInfo) baseModelInfo;
                tvGroup.setText(baseGroupInfo.groupTitle);
            }
        }
    }

    public class ItemChildViewHolder extends ExpandableRecyclerAdapter.ViewHolder {
        TextView tvContemt;

        public ItemChildViewHolder(View view) {
            super(view);
            tvContemt = (TextView) view.findViewById(R.id.tv_content);
        }

        public void bind(final int position) {
            BaseModelInfo baseModelInfo = visibleItems.get(position);
            if (baseModelInfo != null && baseModelInfo instanceof ContentInfo) {
                final ContentInfo contentInfo = (ContentInfo) baseModelInfo;
                tvContemt.setText(contentInfo.getTotal());
            }
        }
    }

}
