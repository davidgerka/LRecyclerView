package com.lzx.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lzx.demo.bean.ContentInfo;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.util.RecyclerViewUtils;
import com.handmark.pulltorefresh.library.BaseGroupInfo;
import com.handmark.pulltorefresh.library.BaseModelInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 可展开/收起的RecyclerView分组列表的通用适配器
 * Created by lqn on 2017/4/26
 */
public abstract class ExpandableRecyclerAdapter<T extends BaseModelInfo> extends RecyclerView.Adapter<ExpandableRecyclerAdapter.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CONTENT = 1;
    public static final int MODE_NORMAL = 0;       //可以展开多个组
    public static final int MODE_ACCORDION = 1;    //只能展开一个组，如果点击另一个组，会收起旧的组，然后展开新的组
    private static final int ARROW_ROTATION_DURATION = 150;
    protected Context mContext;
    protected List<T> allItems = new ArrayList<>();
    protected List<T> visibleItems = new ArrayList<>();
    private List<Integer> indexList = new ArrayList<>();
    private SparseIntArray expandMap = new SparseIntArray();
    private int mode;
    private LayoutInflater mInflater;

    public ExpandableRecyclerAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static void openArrow(View view) {
        view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(180);
    }

    public static void closeArrow(View view) {
        view.animate().setDuration(ARROW_ROTATION_DURATION).rotation(90);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return visibleItems == null ? 0 : visibleItems.size();
    }

    protected View inflate(int resourceID, ViewGroup viewGroup) {
        return mInflater.inflate(resourceID, viewGroup, false);
    }

    public boolean toggleExpandedItems(int position, boolean notify) {
        if (isExpanded(position)) {
            collapseItems(position, notify);
            return false;
        } else {
            expandItems(position, notify);

            if (mode == MODE_ACCORDION) {
                collapseAllExcept(position);
            }

            return true;
        }
    }

    public void expandItems(int position, boolean notify) {
        int count = 0;
        int index = indexList.get(position);
        int insert = position;

        for (int i = index + 1; i < allItems.size() && allItems.get(i).itemType != TYPE_HEADER; i++) {
            insert++;
            count++;
            visibleItems.add(insert, allItems.get(i));
            indexList.add(insert, i);
        }

        notifyItemRangeInserted(position + 1, count);

        int allItemsPosition = indexList.get(position);
        expandMap.put(allItemsPosition, 1);

        if (notify) {
            notifyItemChanged(position);
        }
    }

    public void collapseItems(int position, boolean notify) {
        int count = 0;
        int index = indexList.get(position);

        for (int i = index + 1; i < allItems.size() && allItems.get(i).itemType != TYPE_HEADER; i++) {
            count++;
            visibleItems.remove(position + 1);
            indexList.remove(position + 1);
        }

        notifyItemRangeRemoved(position + 1, count);

        int allItemsPosition = indexList.get(position);
        expandMap.delete(allItemsPosition);

        if (notify) {
            notifyItemChanged(position);
        }
    }

    public boolean isExpanded(int position) {
        int allItemsPosition = indexList.get(position);
        return expandMap.get(allItemsPosition, -1) >= 0;
    }

    @Override
    public int getItemViewType(int position) {
        return visibleItems.get(position).itemType;
    }

    public void setGroupItems(List<BaseGroupInfo<ContentInfo>> groupList){
        ArrayList<BaseModelInfo> items = new ArrayList<BaseModelInfo>();
        int i = groupList.size();
        int j;
        BaseGroupInfo baseGroupInfo;
        BaseModelInfo baseModelInfo;
        for (int n = 0; n < i; n++){
            baseGroupInfo = groupList.get(n);
            items.add(baseGroupInfo);
            if(baseGroupInfo.dataList != null && baseGroupInfo.dataList.size() > 0){
                j = baseGroupInfo.dataList.size();
                for (int m = 0; m < j; m++){
                    baseModelInfo = (BaseModelInfo) baseGroupInfo.dataList.get(m);
                    items.add(baseModelInfo);
                }
            }
        }
        setItems((List<T>)items);
    }

    public void setItems(List<T> items) {
        allItems = items;
        List<T> visibleItems = new ArrayList<>();
        expandMap.clear();
        indexList.clear();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).itemType == TYPE_HEADER) {
                indexList.add(i);
                visibleItems.add(items.get(i));
            }
        }

        this.visibleItems = visibleItems;
        notifyDataSetChanged();
    }

    public int getIndexWithPosition(int position){
        if(position < 0 || visibleItems == null || position >= visibleItems.size()){
            return -1;
        }
        BaseModelInfo baseModelInfo;
        for (int i = position; i >= 0; i--) {
            baseModelInfo = visibleItems.get(i);
            if (baseModelInfo != null && baseModelInfo instanceof BaseGroupInfo) {
                return i;
            }
        }
        return -1;
    }

    public BaseGroupInfo getGroupModelWithIndex(int index){
        if(index < 0 || visibleItems == null || index >= visibleItems.size()){
            return null;
        }
        BaseModelInfo baseModelInfo = visibleItems.get(index);
        if (baseModelInfo != null && baseModelInfo instanceof BaseGroupInfo) {
            return (BaseGroupInfo) baseModelInfo;
        }
        return null;
    }

    /**
     * 获取当前位置的item所属的组的组model
     * @param position  当前位置
     * @return  组model
     */
    public BaseGroupInfo getGroupModelWithPosition(int position){
        if(position < 0 || visibleItems == null || position >= visibleItems.size()){
            return null;
        }
        BaseModelInfo baseModelInfo;
        BaseGroupInfo baseGroupInfo = null;
        for (int i = position; i >= 0; i--) {
            baseModelInfo = visibleItems.get(i);
            if (baseModelInfo != null && baseModelInfo instanceof BaseGroupInfo) {
                baseGroupInfo = (BaseGroupInfo) baseModelInfo;
                baseGroupInfo.hasExpand = isExpanded(i);
                break;
            }
        }
        return baseGroupInfo;
    }

    protected void notifyItemInserted(int allItemsPosition, int visiblePosition) {
        incrementIndexList(allItemsPosition, visiblePosition, 1);
        incrementExpandMapAfter(allItemsPosition, 1);

        if (visiblePosition >= 0) {
            notifyItemInserted(visiblePosition);
        }
    }

    protected void removeItemAt(int visiblePosition) {
        int allItemsPosition = indexList.get(visiblePosition);

        allItems.remove(allItemsPosition);
        visibleItems.remove(visiblePosition);

        incrementIndexList(allItemsPosition, visiblePosition, -1);
        incrementExpandMapAfter(allItemsPosition, -1);

        notifyItemRemoved(visiblePosition);
    }

    private void incrementExpandMapAfter(int position, int direction) {
        SparseIntArray newExpandMap = new SparseIntArray();

        for (int i = 0; i < expandMap.size(); i++) {
            int index = expandMap.keyAt(i);
            newExpandMap.put(index < position ? index : index + direction, 1);
        }

        expandMap = newExpandMap;
    }

    private void incrementIndexList(int allItemsPosition, int visiblePosition, int direction) {
        List<Integer> newIndexList = new ArrayList<>();

        for (int i = 0; i < indexList.size(); i++) {
            if (i == visiblePosition) {
                if (direction > 0) {
                    newIndexList.add(allItemsPosition);
                }
            }

            int val = indexList.get(i);
            newIndexList.add(val < allItemsPosition ? val : val + direction);
        }

        indexList = newIndexList;
    }

    public void collapseAll() {
        collapseAllExcept(-1);
    }

    public void collapseAllExcept(int position) {
        for (int i = visibleItems.size() - 1; i >= 0; i--) {
            if (i != position && getItemViewType(i) == TYPE_HEADER) {
                if (isExpanded(i)) {
                    collapseItems(i, true);
                }
            }
        }
    }

    public void expandAll() {
        for (int i = visibleItems.size() - 1; i >= 0; i--) {
            if (getItemViewType(i) == TYPE_HEADER) {
                if (!isExpanded(i)) {
                    expandItems(i, true);
                }
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }

    public class HeaderViewHolder extends ViewHolder {
        ImageView arrow;
        LRecyclerView recyclerView;

        /**
         * 创建HeaderViewHolder,这里需要注意，如果Header没有表示展开与收缩的箭头图片，arrow参数就填null
         *
         * @param view
         * @param arrow
         * @param recyclerView
         */
        public HeaderViewHolder(View view, final ImageView arrow, final LRecyclerView recyclerView) {
            super(view);

            this.arrow = arrow;
            this.recyclerView = recyclerView;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick();
                }
            });
        }

        protected void handleClick() {
            if (toggleExpandedItems(RecyclerViewUtils.getLayoutPosition(recyclerView, this), false)) {
                openArrow(arrow);
            } else {
                closeArrow(arrow);
            }
        }

        public void bind(int position) {
            if (arrow != null) {
                arrow.setRotation(isExpanded(position) ? 180 : 90);
            }
        }
    }

    public class StaticViewHolder extends ViewHolder {
        public StaticViewHolder(View view) {
            super(view);
        }
    }

    public class ItemViewHolder extends ViewHolder {
        public ItemViewHolder(View view) {
            super(view);
        }
    }

    public static class ListItem extends BaseModelInfo{
        public int ItemType;

        public ListItem(int itemType) {
            ItemType = itemType;
        }
    }
}
