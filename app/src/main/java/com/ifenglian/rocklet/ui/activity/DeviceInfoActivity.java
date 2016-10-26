package com.ifenglian.rocklet.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.util.BluetoothDeviceManage;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.util.Config;

import java.util.concurrent.Executors;

/**
 * 设备详情页面
 */
public class DeviceInfoActivity extends AppCompatActivity  implements View.OnClickListener {

    private TextView dNameText, versionText, rssiText, callText, levelText, levelvalueText;
    private ImageView headImage;
    private ImageView battery_levelImage;
    private ImageView call_image;
    private String mac;
    private BluetoothDeviceManage deviceManage;
    private Device device;
    private boolean readRssi = true;
    private boolean isCalling = false;
    private int position;
    private int rssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        Intent myIntent = getIntent();
        position = myIntent.getIntExtra("position",0);
        mac = myIntent.getStringExtra("mac");
        BluetoothDeviceDB deviceDataBase = BluetoothDeviceDB.getInstance(this);
        device = deviceDataBase.getDevByMac(mac);
        deviceDataBase.CloseDb();
        if (device == null) {
            finish();
        }
        if (Config.deviceManages.size() > 0) {
            deviceManage = Config.deviceManages.get(position);
            deviceManage.checkConnectionRssi();
            rssi = deviceManage.getRssi();
        }
        initView();
        setRssiView();
        //每隔2秒读取一次设备的rssi信号
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (readRssi) {
                        Thread.sleep(2000);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                rssi = deviceManage.getRssi();
                                setRssiView();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initView() {
        ImageView back = (ImageView) findViewById(R.id.back);
        dNameText = (TextView) findViewById(R.id.DNameText);
        versionText = (TextView) findViewById(R.id.vText);
        rssiText = (TextView) findViewById(R.id.rssiText);
        callText = (TextView) findViewById(R.id.call_text);
        levelText = (TextView) findViewById(R.id.level_text);
        levelText.setVisibility(View.GONE);
        headImage = (ImageView) findViewById(R.id.headImage);
        battery_levelImage = (ImageView) findViewById(R.id.level_image);
        call_image = (ImageView) findViewById(R.id.call_image);
        ImageView updateName_Img = (ImageView) findViewById(R.id.updateName_Img);
        Button deleteButton = (Button) findViewById(R.id.delete_Button);
        levelvalueText = (TextView) findViewById(R.id.level_textvalue1);
        //呼叫和停止呼叫设备
        call_image.setOnClickListener(this);
        updateName_Img.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void setRssiView() {
        if (rssi > -45) {
            rssiText.setText(getString(R.string.high));
        } else if (rssi > -60) {
            rssiText.setText(R.string.middle);
        } else if (rssi < -80) {
            rssiText.setText(R.string.weak);
        }
        rssiText.setTextColor(0xff444444);
        if (rssi == -200) {
            //已断开
            call_image.setEnabled(false);
            rssiText.setTextColor(0xfffc4e4e);
            rssiText.setText(getString(R.string.stopConnect));
            call_image.setBackgroundResource(R.drawable.call_hui);
        } else {
            call_image.setEnabled(true);
            call_image.setBackgroundResource(R.drawable.call);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothDeviceDB deviceDataBase = BluetoothDeviceDB.getInstance(this);
        device = deviceDataBase.getDevByMac(mac);
        deviceDataBase.CloseDb();
        if (device == null) {
            finish();
        }
        battery_levelImage.setVisibility(View.INVISIBLE);
        headImage.setImageBitmap(device.getHeadImage());
        dNameText.setText(device.getName());
        versionText.setText(device.getEdition());
        int electricity = Integer.parseInt(device.getElectricity());
        if (electricity <= 15) {
            battery_levelImage.setImageResource(R.drawable.battery_low);
            battery_levelImage.setVisibility(View.VISIBLE);
            levelText.setVisibility(View.VISIBLE);
            levelvalueText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        readRssi = false;
        if (isCalling) {
            deviceManage.stopCall();
        }
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    private void deleteDevice() {
        readRssi = false;
        if (isCalling) {
            deviceManage.stopCall();
        }
        int id = Integer.parseInt(device.getId());
        BluetoothDeviceDB.getInstance(this).DeleteDevById(id);
        BluetoothDeviceDB.getInstance(this).CloseDb();
        deviceManage.clearDevIdMsg();
        Config.deviceManages.remove(position);
        Config.devices.remove(position);
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.updateName_Img:
                Intent intent = new Intent(DeviceInfoActivity.this, AddOrModifyDeviceActivity.class);
                intent.putExtra("isNewDeviceEdit", false);
                intent.putExtra("mac", mac);
                startActivity(intent);
                break;
            case R.id.back:
                readRssi = false;
                if (isCalling) {
                    deviceManage.stopCall();
                }
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                break;
            case R.id.delete_Button:
                LayoutInflater inflater = LayoutInflater.from(DeviceInfoActivity.this);
                final View view = inflater.inflate(
                        R.layout.activity_del, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
                builder.setCancelable(false);
                builder.setView(view);
                Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                Button sureBtn = (Button) view.findViewById(R.id.sureBtn);
                final AlertDialog dialog = builder.show();
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                sureBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //确认删除
                        dialog.dismiss();
                        deleteDevice();
                    }
                });
                break;
            case R.id.call_image:
                if (!isCalling) {
                    isCalling = true;
                    deviceManage.call();
                    callText.setText(getString(R.string.closecall));
                } else {
                    isCalling = false;
                    deviceManage.stopCall();
                    callText.setText(getString(R.string.call));
                }
                break;
        }
    }
}
