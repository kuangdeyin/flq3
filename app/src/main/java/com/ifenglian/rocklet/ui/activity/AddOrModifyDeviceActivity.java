package com.ifenglian.rocklet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.ui.fragment.AddOrModifyDeviceFragment;
import com.ifenglian.rocklet.ui.fragment.No_resultFragment;
import com.ifenglian.rocklet.ui.fragment.SearchFragment;

/**
 * 添加设备(修改设备信息)页面
 */
public class AddOrModifyDeviceActivity extends AppCompatActivity implements SearchFragment.ResultListener,
         No_resultFragment.ReAddClick {

    private FragmentManager fragmentManager = null;
    private String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        ImageView add_device_back = (ImageView) findViewById(R.id.add_device_back);
        add_device_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        boolean isNewDeviceEdit = getIntent().getBooleanExtra("isNewDeviceEdit", true);
        mac = getIntent().getStringExtra("mac");
        isNewDeviceEdit(isNewDeviceEdit);
    }

    private void isNewDeviceEdit(boolean enable) {
        if (enable) {
            replaceFragment(R.id.add_frameLayout, new SearchFragment());
        } else {
            replaceFragment(R.id.add_frameLayout, AddOrModifyDeviceFragment.newInstance(mac, null));
        }
    }

    private void replaceFragment(int resource, Fragment fragment) {
        if (fragmentManager==null) {
            fragmentManager = getSupportFragmentManager();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (TextUtils.isEmpty(mac)) {
            fragmentTransaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out, R.anim.push_right_in,R.anim.push_right_out);
        }
        fragmentTransaction.replace(resource, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void searchResult(int actionCode, Device pDeviceBin) {
        if (actionCode == 1) {
            replaceFragment(R.id.add_frameLayout, AddOrModifyDeviceFragment.newInstance("", pDeviceBin));
        } else {
            replaceFragment(R.id.add_frameLayout, new No_resultFragment());
        }
    }

    @Override
    public void click() {
        replaceFragment(R.id.add_frameLayout, new SearchFragment());
    }
}
