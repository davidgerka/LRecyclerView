package com.lzx.demo.bean;

import com.handmark.pulltorefresh.library.BaseGroupInfo;
import com.handmark.pulltorefresh.library.BaseModelInfo;

import java.util.ArrayList;
import java.util.List;

public class StickyGroupExpandDataManager {
    public void ResetManagerData() {
        dataList.clear();
    }




    //更新战绩首页数据集
    public List<BaseGroupInfo<BaseModelInfo>> dataList = new ArrayList();

    private static final int GROUP_ITEMTYPE = 0;
    private static final int CONTENT_ITEMTYPE = 1;

    public void refreshRecordList() {
        clearList();
        addRecordList();
    }

    public void addRecordList(){
        ContentInfo info = null;
        List<BaseModelInfo> contentInfoList = null;
        int i = dataList.size();
        int k = i + 1;
        int n = 30;
        for (; i < k; i++) {
//            n++;
            contentInfoList = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                info = new ContentInfo();
                info.itemType = CONTENT_ITEMTYPE;
                info.setTotal("第" + i + "组,第" + j + "个子view");
                contentInfoList.add(info);

            }
            dataList.add(new BaseGroupInfo<BaseModelInfo>(GROUP_ITEMTYPE, null, "第" + i + "组", contentInfoList));
        }
    }

    public void clearList(){
        dataList.clear();
    }
}




