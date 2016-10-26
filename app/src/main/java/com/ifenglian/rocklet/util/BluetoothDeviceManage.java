package com.ifenglian.rocklet.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.ui.activity.CallActivity;
import com.ifenglian.rocklet.ui.activity.LostActivity;
import com.ifenglian.rocklet.util.set.MusicUtil;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by licy on 2015/12/29.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothDeviceManage {

    private long callTime = 0;
    private Context context;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager manager;
    private BluetoothAdapter bluetoothAdapter;
    private int rssi = -200;
    private int battLevel;
    private String firmwareVersion = "v_2.2.8";
    private boolean deleteDevice = false;
    private String mac;
    private static final int MSG_CHECK_CONN_RELIABLE = 0;
    private static final int READ_RSSI = 5;
    private static final int MSG_DISCONNECTED = 1;
    private static final int CONNECT_DEVICE = 3;
    private isConnectSuccess isConnectSuccess;
    private boolean isAdding = false;
    private boolean device_lost = false;
    private boolean safety_readRssi = false;
    private boolean bluetoothConnected = false;
    private MyHandler handler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<BluetoothDeviceManage> reference;

        public MyHandler(BluetoothDeviceManage manage) {
            reference = new WeakReference<BluetoothDeviceManage>(manage);
        }

        public void handleMessage(Message msg) {
            BluetoothDeviceManage deviceManage = reference.get();
            switch (msg.what) {
                case MSG_CHECK_CONN_RELIABLE:
                    if (deviceManage.isAdding) {
                        deviceManage.close();
                        deviceManage.isConnectSuccess.addFail();
                    } else {
                        deviceManage.connect(deviceManage.mac);
                        sendEmptyMessageDelayed(MSG_CHECK_CONN_RELIABLE, 20 * 1000);
                    }
                    break;
                case CONNECT_DEVICE:
                    if (Config.BluetoothOpen) {
                        //蓝牙打开
                        deviceManage.connect(deviceManage.mac);
                        //隔25秒再发送MSG_CHECK_CONN_RELIABLE
                        sendEmptyMessageDelayed(MSG_CHECK_CONN_RELIABLE, 25 * 1000);
                    }
                    break;
                case MSG_DISCONNECTED:
                    deviceManage.lostDevice();
                    break;
                case READ_RSSI:
                    if (deviceManage.bluetoothGatt != null) {
                        if (deviceManage.bluetoothConnected)
                            deviceManage.bluetoothGatt.readRemoteRssi();
                    }
                    break;
            }
        }
    }

    ;

    /**
     * 监听设备是否连接成功
     */
    public interface isConnectSuccess {

        void addSuccess();

        void addFail();
    }

    private BluetoothGattCallback callbackListener = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("test", "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID uuid = characteristic.getUuid();
                byte[] rawValue = characteristic.getValue();
                if (uuid.equals(UUidUtil.bondIdMsgReadCharUUID)) {
                    PhoneOnlyId id = new PhoneOnlyId(rawValue);
                    //设备没有被该手机绑定过
                    if (!id.equals(APPUtil.getOnlyId(context)) && !id.isBlankId()) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (isAdding) {
                                    isConnectSuccess.addFail();
                                }
                                close();
                                //设备已被绑定！请添加未绑定设备
                                Toast toast = Toast.makeText(context, R.string.isConnected, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        });
                    } else {
                        //连接成功
                        //添加设备3
                        bluetoothConnected = true;
                        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.bondIdMsgSetCharUUID);
                        if (gattCharacteristic != null) {
                            gattCharacteristic.setValue(APPUtil.getOnlyId(context).getBytes());
                            bluetoothGatt.writeCharacteristic(gattCharacteristic);
                        }
                    }
                } else if (uuid.equals(UUidUtil.bondIdMsgSetCharUUID)) {
                    //添加设备5
                    PhoneOnlyId id = new PhoneOnlyId(rawValue);
                    if (id.equals(APPUtil.getOnlyId(context))) {
                        //读取设备电量
                        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.battSerUUID, UUidUtil.battLevelUUID);
                        if (gattCharacteristic != null)
                            bluetoothGatt.readCharacteristic(gattCharacteristic);
                    }
                    checkConnectionRssi();
                    if (!isAdding) {
                        isConnectSuccess.addSuccess();
                        setDeviceStateByMac(1);
                        checkDeviceConnect();
                    }
                } else if (uuid.equals(UUidUtil.battLevelUUID)) {
                    battLevel = rawValue[0];
                    //读取设备版本
                    BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.devInfoSerUUID, UUidUtil.firVersionUUID);
                    if (gattCharacteristic != null)
                        bluetoothGatt.readCharacteristic(gattCharacteristic);
                } else if (uuid.equals(UUidUtil.firVersionUUID)) {
                    firmwareVersion = new String(rawValue);
                    BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.notifyReadCharUUID);
                    if (gattCharacteristic != null) {
                        //设置当指定characteristic值变化时，发出通知
                        if (bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true)) {
                            BluetoothGattDescriptor descriptor = gattCharacteristic
                                    .getDescriptor(UUidUtil.notifyDescriptorUUID);
                            if (descriptor != null) {
                                byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                                if (descriptor.setValue(val))
                                    bluetoothGatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                    if (isAdding)
                        isConnectSuccess.addSuccess();
                    else {
                        BluetoothDeviceDB deviceDataBase = BluetoothDeviceDB.getInstance(context);
                        deviceDataBase.UpdateEleAndEditionByMac(mac, battLevel + "", firmwareVersion);
                        deviceDataBase.CloseDb();
                    }
                    isAdding = false;
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("test", "onCharacteristicWrite");
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    UUID uuid = characteristic.getUuid();
                    byte[] rawValue = characteristic.getValue();
                    if (uuid.equals(UUidUtil.bondIdMsgSetCharUUID)) {
                        //添加设备4
                        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.bondIdMsgSetCharUUID);
                        if (gattCharacteristic != null)
                            bluetoothGatt.readCharacteristic(gattCharacteristic);
                    } else if (uuid.equals(UUidUtil.linkLossStateUUID))
                        //设备丢失
                        device_lost = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("test", "onCharacteristicChanged");
            //设备呼叫手机
            long time = System.currentTimeMillis();
            if (callTime == 0) {
                callTime = System.currentTimeMillis();
                showCallDialog(gatt);
            } else {
                if (time - callTime > 3000) {
                    //两次呼叫间隔大于3秒
                    callTime = System.currentTimeMillis();
                    showCallDialog(gatt);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //添加设备1
            Log.d("test", "onConnectionStateChange");
            try {
                handler.removeMessages(MSG_CHECK_CONN_RELIABLE);
                final String mac = gatt.getDevice().getAddress();
                if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS)
                    bluetoothGatt.discoverServices();
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    device_lost = false;
                    if (!deleteDevice) {
                        if (isAdding)
                            //添加失败
                            isConnectSuccess.addFail();
                        else {
                            connectDevice(mac);
                            setDeviceStateByMac(0);
                            isConnectSuccess.addFail();
                            if (bluetoothConnected) {
                                //设备丢失
                                bluetoothConnected = false;
                                handler.sendEmptyMessageDelayed(MSG_DISCONNECTED, 3 * 1000);
                            }
                        }
                    } else {
                        setRssi(-200);
                        close();
                        //设置设备的state为2
                        setDeviceStateByMac(2);
                    }
                } else {
                    device_lost = false;
                    isConnectSuccess.addFail();
                    setDeviceStateByMac(0);
                    connectDevice(mac);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d("test", "onReadRemoteRssi");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //每隔2秒读取一次rssi信号
                checkConnectionRssi();
                if (rssi > -80) {
                    safety_readRssi = true;
                    if (!device_lost) {
                        //当信号大于-80才写入设备丢失提醒
                        if (bluetoothConnected) {
                            byte[] val = new byte[1];
                            val[0] = 0;
                            BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.linkLossStateUUID);
                            if (gattCharacteristic != null) {
                                gattCharacteristic.setValue(val);
                                bluetoothGatt.writeCharacteristic(gattCharacteristic);
                            }
                        }
                    }
                }
                setRssi(rssi);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //添加设备2
            Log.d("test", "onServicesDiscovered");
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.bondIdMsgReadCharUUID);
                    if (gattCharacteristic != null)
                        bluetoothGatt.readCharacteristic(gattCharacteristic);
                }
            } catch (Exception e) {
                if (isAdding) {
                    isConnectSuccess.addFail();
                }
                e.printStackTrace();
            }
        }
    };

    public BluetoothDeviceManage(Context context, boolean isAdding) {
        this.context = context;
        this.isAdding = isAdding;
    }

    public void init() {
        if (manager == null) {
            manager = (BluetoothManager) context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (manager == null) {
                return;
            }
        }
        bluetoothAdapter = manager.getAdapter();
        if (bluetoothAdapter == null) {
            return;
        }
    }

    public int getBattLevel() {
        return battLevel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int mRssi) {
        rssi = mRssi;
    }

    public void connectDevice(final String mac) {
        this.mac = mac;
        handler.sendEmptyMessageDelayed(CONNECT_DEVICE, 100);
    }

    public void setIsConnectSuccess(isConnectSuccess success) {
        isConnectSuccess = success;
    }

    /**
     * 连接设备
     *
     * @param address
     * @return
     */
    private boolean connect(final String address) {
        try {
            close();
            android.bluetooth.BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                return false;
            }
            bluetoothGatt = device.connectGatt(context, false, callbackListener);
            if (bluetoothGatt == null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 关闭GATT Client端。
     */
    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /**
     * 呼叫设备
     */
    public void call() {
        byte[] val = new byte[1];
        val[0] = 2;
        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.mediaAlrtSerUUID, UUidUtil.mediaAlrtLevelUUID);
        if (gattCharacteristic != null) {
            gattCharacteristic.setValue(val);
            bluetoothGatt.writeCharacteristic(gattCharacteristic);
        }
    }

    /**
     * 停止呼叫
     */
    public void stopCall() {
        byte[] val = new byte[1];
        val[0] = 0;
        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.mediaAlrtSerUUID, UUidUtil.mediaAlrtLevelUUID);
        if (gattCharacteristic != null) {
            gattCharacteristic.setValue(val);
            bluetoothGatt.writeCharacteristic(gattCharacteristic);
        }
    }

    /**
     * 清除设备绑定信息
     */
    public void clearDevIdMsg() {
        deleteDevice = true;
        byte[] val = new byte[1];
        val[0] = 0;
        BluetoothGattCharacteristic gattCharacteristic = getGattChar(bluetoothGatt, UUidUtil.privateSerUUID, UUidUtil.clearBondMsgCharUUID);
        if (gattCharacteristic == null)
            return;
        gattCharacteristic.setValue(val);
        bluetoothGatt.writeCharacteristic(gattCharacteristic);
        close();
    }

    /**
     * 监听信号强度
     *
     * @return
     */
    public void checkConnectionRssi() {
        handler.sendEmptyMessageDelayed(READ_RSSI, 2 * 1000);
    }

    /**
     * 设备丢失
     */
    private void lostDevice() {
        if (!bluetoothConnected) {
            if (safety_readRssi) {
                //判断是否回到安全连接区
                safety_readRssi = false;
                //根据保存数据中的连接状态循环查找是否还有其他设备丢失
                Config.lostNameList.clear();
                for (Device device : Config.devices) {
                    if (device.getState() == 0) {
                        Config.lostNameList.add(device.getName());
                    }
                }
                BluetoothDeviceDB.getInstance(context).CloseDb();
                if (Config.lostActivity != null) {
                    Config.lostActivity.finish();
                    Config.lostActivity = null;
                }
                if (Config.BluetoothOpen) {
                    //手机蓝牙打开
                    MusicUtil.stop();
                    Intent intent = new Intent(context, LostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
            setRssi(-200);
        }
    }

    /**
     * 检查设备重新连接后是否还有设备丢失有就继续提示，无就关闭提示框
     */
    private void checkDeviceConnect() {
        if (Config.lostActivity != null) {
            Config.lostNameList.clear();
            //查找是否还有其他设备丢失
            for (int i = 0; i < Config.devices.size(); i++) {
                if (Config.devices.get(i).getState() == 0) {
                    Config.lostNameList.add(Config.devices.get(i).getName());
                }
            }
            if (Config.lostNameList.size() > 0) {
                if (Config.lostActivity != null) {
                    Config.lostActivity.finish();
                    Config.lostActivity = null;
                }
                Intent intent = new Intent(context, LostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                if (Config.lostActivity != null) {
                    Config.lostActivity.finish();
                    Config.lostActivity = null;
                }
            }
            MusicUtil.stop();
        }
    }

    /**
     * 设弹出呼叫界面
     *
     * @param gatt
     */
    private void showCallDialog(BluetoothGatt gatt) {
        MusicUtil.stop();
        if (!TextUtils.isEmpty(Config.call_mac)) {
            if (Config.call_mac.equals(gatt.getDevice().getAddress())) {
                //已经弹出呼叫界面，同一个设备再次呼叫，关闭呼叫界面
                Config.call_mac = null;
                Config.callActivity.finish();
                Config.callActivity = null;
                return;
            }
        }
        Config.call_mac = gatt.getDevice().getAddress();
        Intent intent = new Intent(context, CallActivity.class);
        BluetoothDeviceDB dataBase = BluetoothDeviceDB.getInstance(context);
        if (dataBase.isTableExits()) {
            Device device = dataBase.getDevByMac(mac);
            dataBase.CloseDb();
            intent.putExtra("name", device.getName());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 设置设备连接状态
     *
     * @param state
     */
    private void setDeviceStateByMac(int state) {
        if (Config.devices.size() > 0) {
            for (Device device : Config.devices) {
                if (device.getMac().equals(mac)) {
                    device.setState(state);
                    break;
                }
            }
        }
    }

    private BluetoothGattCharacteristic getGattChar(BluetoothGatt bluetoothGatt, UUID servUUID, UUID charUUID) {
        if (bluetoothGatt == null)
            return null;
        BluetoothGattService gattService = bluetoothGatt.getService(servUUID);
        if (gattService == null)
            return null;
        return gattService.getCharacteristic(charUUID);
    }
}
