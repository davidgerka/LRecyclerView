package com.lzx.demo.bean;

import com.handmark.pulltorefresh.library.BaseModelInfo;

import java.io.Serializable;


public class ContentInfo extends BaseModelInfo implements Serializable {
    String total;
    String up;


    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }


    public void setUp(String up) {
        this.up = up;
    }


}
