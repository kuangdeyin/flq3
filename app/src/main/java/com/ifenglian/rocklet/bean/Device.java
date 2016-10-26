package com.ifenglian.rocklet.bean;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by licy on 2015/11/16.
 */
public class Device implements Serializable {

    private String id;//列表Id
    private String name;//蓝牙名称
    private String mac;//设备mac地址
    private Bitmap headImage;//头像
    private String electricity;//设备电量
    private String edition;//设备版本号
    private int state; //设备连接状态(0:已断开,1:已连接,其他:连接中)
    private int rssi;//信号强度

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Bitmap getHeadImage() {
        return headImage;
    }

    public void setHeadImage(Bitmap headImage) {
        this.headImage = headImage;
    }

    public String getElectricity() {
        return electricity;
    }

    public void setElectricity(String electricity) {
        this.electricity = electricity;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }
}
