package com.ifenglian.rocklet.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.util.BluetoothDeviceManage;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.util.Config;
import com.ifenglian.rocklet.util.set.MusicUtil;

import java.util.List;

/**
 * Created by licy on 2015/12/15.
 */
public class BluetoothDeviceService extends Service {

    static BluetoothDeviceManage.isConnectSuccess isConnectSuccess;

    //监听手机状态
    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            //开屏
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null
                        && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    //wifi网络
                    //上传Log信息
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                //关屏
                MusicUtil.stop();
            } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                //手机电量
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                Config.PhoneLevel = level + "";
            } else {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                String msg = null;
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        msg = "turning on";
                        break;
                    case BluetoothAdapter.STATE_ON:
                        initDevices();
                        if (Config.OpenBleActivity != null) {
                            Config.OpenBleActivity.finish();
                        }
                        Config.BluetoothOpen = true;
                        msg = "on";
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Config.BluetoothOpen = false;
                        msg = "turning off";
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Config.BluetoothOpen = false;
                        msg = "off";
                        break;
                }
            }
        }
    };

    public static void setIsConnectSuccess(BluetoothDeviceManage.isConnectSuccess success) {
        isConnectSuccess = success;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//注册监听蓝牙
        filter.addAction(Intent.ACTION_SCREEN_OFF);//监听关屏
        filter.addAction(Intent.ACTION_SCREEN_ON);//监听开屏
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//监听手机电量
        registerReceiver(stateChangeReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initDevices();
        return START_STICKY;
    }

    public void initDevices() {
        if (BluetoothDeviceDB.getInstance(this).isTableExits()) {
            List<Device> deviceList = BluetoothDeviceDB.getInstance(this).getDevs();
            BluetoothDeviceDB.getInstance(this).CloseDb();
            Config.deviceManages.clear();
            Config.devices.clear();
            for (int i=0;i<deviceList.size();i++) {
                BluetoothDeviceManage manage = new BluetoothDeviceManage(this, false);
                Device device=deviceList.get(i);
                device.setState(2);
                Config.devices.add(device);
                manage.init();
                //连接设备
                manage.connectDevice(device.getMac());
                Config.deviceManages.add(i,manage);
            }
            if (Config.deviceManages.size() > 0) {
                for (BluetoothDeviceManage manage : Config.deviceManages) {
                    if (isConnectSuccess != null)
                        manage.setIsConnectSuccess(isConnectSuccess);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (stateChangeReceiver != null) {
            unregisterReceiver(stateChangeReceiver);
        }
        stopForeground(true);
    }
}
