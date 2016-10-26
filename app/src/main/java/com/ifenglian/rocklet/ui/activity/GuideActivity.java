package com.ifenglian.rocklet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.ui.adapter.GuidePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导页
 */
public class GuideActivity extends Activity implements OnPageChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        if(BluetoothDeviceDB.getInstance(this).isTableExits()){
            Intent intent = new Intent(this,DeviceManageActivty.class);
            startActivity(intent);
            finish();
        } else {
            initViews();
        }
    }

    private void initViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        List<View> views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.what_new_one, null));
        views.add(inflater.inflate(R.layout.what_new_two, null));
        GuidePagerAdapter adapter = new GuidePagerAdapter(views, this);
        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(adapter);
        vp.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int arg0) {

    }
}
