package com.lzx.demo.bean;

import com.handmark.pulltorefresh.library.BaseGroupInfo;

import java.util.ArrayList;
import java.util.List;

public class StickyGroupExpandDataManager {
    public void ResetManagerData() {
        dataList.clear();
    }




    //更新战绩首页数据集
    public List<BaseGroupInfo<ContentInfo>> dataList = new ArrayList();

    private static final int GROUP_ITEMTYPE = 0;
    private static final int CONTENT_ITEMTYPE = 1;

    public void updateRecordList(String pListData) {
        dataList.clear();
        ContentInfo info = null;
        List<ContentInfo> contentInfoList = null;

        for (int i = 0; i < 5; i++) {

            contentInfoList = new ArrayList<>();
            for (int j = 0; j < i + 2; j++) {
                info = new ContentInfo();
                info.itemType = CONTENT_ITEMTYPE;
                info.setTotal("第" + i + "组,第" + j + "个子view");
                contentInfoList.add(info);

            }
            dataList.add(new BaseGroupInfo<ContentInfo>(GROUP_ITEMTYPE, null, "第" + i + "组", contentInfoList));
        }

    }
}




