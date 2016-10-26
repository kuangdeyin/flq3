package com.ifenglian.rocklet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ifenglian.module.update.ApkUpdate;
import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.ui.activity.set.SetActivity;
import com.ifenglian.rocklet.ui.activity.set.TimeActivity;
import com.ifenglian.rocklet.util.BluetoothDeviceManage;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.services.BluetoothDeviceService;
import com.ifenglian.rocklet.ui.adapter.ManagerAdapter;
import com.ifenglian.rocklet.util.APPUtil;
import com.ifenglian.rocklet.util.Config;
import com.ifenglian.rocklet.util.SpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备管理页面
 */
public class DeviceManageActivty extends Activity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private LinearLayout add_ll, add_big_ll;
    private ImageView left_image;
    private ImageView set_img;
    private List<Device> deviceList = new ArrayList<Device>();
    private Handler handler = new Handler();
    private ManagerAdapter managerAdapter;
    private BluetoothDeviceManage.isConnectSuccess isConnectSuccess = new BluetoothDeviceManage.isConnectSuccess() {
        public void addSuccess() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (managerAdapter != null) {
                        managerAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        public void addFail() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (managerAdapter != null) {
                        managerAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manager);
        initView();
        //检查版本更新
        ApkUpdate flqUpdate = new ApkUpdate(this);
        flqUpdate.update();
        //检查蓝牙是否打开
        APPUtil.checkBluetoothOpen(this);
        Intent intent = new Intent(this, BluetoothDeviceService.class);
        startService(intent);
        BluetoothDeviceService.setIsConnectSuccess(isConnectSuccess);
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ImageView add_img = (ImageView) findViewById(R.id.add_img);
        add_ll = (LinearLayout) findViewById(R.id.add_ll);
        add_big_ll = (LinearLayout) findViewById(R.id.add_big_ll);
        left_image = (ImageView) findViewById(R.id.left_image);
        set_img = (ImageView) findViewById(R.id.set_img);
        managerAdapter = new ManagerAdapter(deviceList);
        managerAdapter.setOnRecyclerViewListener(new ManagerAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(DeviceManageActivty.this, DeviceInfoActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("mac", Config.devices.get(position).getMac());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in,
                        R.anim.slide_left_out);
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }
        });
        recyclerView.setAdapter(managerAdapter);
        //添加设备
        add_big_ll.setOnClickListener(this);
        //添加设备
        add_img.setOnClickListener(this);
        //设置
        set_img.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        DeviceNotExist();
        if (BluetoothDeviceDB.getInstance(this).isTableExits()) {
            deviceList.clear();
            deviceList = BluetoothDeviceDB.getInstance(this).getDevs();
            BluetoothDeviceDB.getInstance(this).CloseDb();
            if (deviceList.size() > 0) {
                DeviceExist();
            }
        }
        super.onResume();
    }

    /**
     * 有设备存在
     */
    private void DeviceExist() {
        recyclerView.setVisibility(View.VISIBLE);
        add_ll.setVisibility(View.VISIBLE);
        if (deviceList.size() == 6) {
            add_ll.setVisibility(View.GONE);
        }
        set_img.setVisibility(View.VISIBLE);
        add_big_ll.setVisibility(View.GONE);
        left_image.setVisibility(View.VISIBLE);
        left_image.setImageResource(R.drawable.time);
        left_image.setOnClickListener(this);
        managerAdapter.notifyDataSetChanged();
    }

    /**
     * 没连接设备
     */
    private void DeviceNotExist() {
        recyclerView.setVisibility(View.GONE);
        add_ll.setVisibility(View.GONE);
        left_image.setVisibility(View.GONE);
        set_img.setVisibility(View.GONE);
        add_big_ll.setVisibility(View.VISIBLE);
    }

    /**
     * 添加设备
     */
    private void addDevice() {
        Intent intent = new Intent(DeviceManageActivty.this, AddOrModifyDeviceActivity.class);
        intent.putExtra("isNewDeviceEdit", true);
        intent.putExtra("mac", "");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in,
                R.anim.slide_left_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_image:
                if (TextUtils.isEmpty(SpUtils.getString(DeviceManageActivty.this, "silent_time"))) {
                    //设置临时勿扰时间
                    Intent intent = new Intent(DeviceManageActivty.this, TimeActivity.class);
                    startActivity(intent);
                } else {
                    //取消临时勿扰
                    APPUtil.setTime(DeviceManageActivty.this, "");
                    Toast.makeText(DeviceManageActivty.this, "已取消临时勿扰", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.set_img:
                startActivity(new Intent(DeviceManageActivty.this, SetActivity.class));
                overridePendingTransition(R.anim.slide_right_in,
                        R.anim.slide_left_out);
                break;
            case R.id.add_img:
            case R.id.add_big_ll:
                addDevice();
        }
    }
}
