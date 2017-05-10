package com.handmark.pulltorefresh.library;

import java.util.List;


public class BaseGroupInfo<T extends BaseModelInfo> extends BaseModelInfo {
    public boolean hasExpand = true;
    public Object groupItem;
    public String groupTitle;
    public List<T> dataList;

    public BaseGroupInfo(String title, List<T> dataList) {
        this.groupTitle = title;
        this.dataList = dataList;
    }

    public BaseGroupInfo(int itemType, Object groupItem, String title, List<T> dataList) {
        this.itemType = itemType;
        this.groupItem = groupItem;
        this.groupTitle = title;
        this.dataList = dataList;
    }

    public BaseGroupInfo<T> SetHasExpand(boolean hasExpand) {
        this.hasExpand = hasExpand;
        return this;
    }
}
