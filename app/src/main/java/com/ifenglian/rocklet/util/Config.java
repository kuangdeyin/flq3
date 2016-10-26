package com.ifenglian.rocklet.util;

import android.app.Activity;

import com.ifenglian.rocklet.bean.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/10.
 */
public class Config {

    public static List<BluetoothDeviceManage> deviceManages = new ArrayList<BluetoothDeviceManage>();//已经添加成功的设备缓存

    public static List<Device> devices = new ArrayList<Device>();//保存设备连接状态

    public static String call_mac = null;//呼叫设备的mac地址

    public static Activity callActivity = null;//呼叫设备Activity

    public static String PhoneLevel;//手机电量

    public static boolean BluetoothOpen = false;//监听蓝牙状态

    public static List<String> lostNameList = new ArrayList<String>();//保存丢失设备名称的list

    public static Activity lostActivity = null;//丢失设备Activity

    public static Activity timeActivity = null;//静默时间Activity

    public static Activity OpenBleActivity = null;//蓝牙提示页

}
