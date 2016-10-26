package com.ifenglian.rocklet.ui.activity;


import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.Config;
import com.ifenglian.rocklet.util.SpUtils;
import com.ifenglian.rocklet.util.set.MusicUtil;
import com.ifenglian.rocklet.util.ScreenUtil;

/**
 * 丢失弹出页面
 */
public class LostActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lost);
        getWindow().setBackgroundDrawable(new BitmapDrawable());
        Config.lostActivity = this;
        if (Config.callActivity != null) {
            Config.callActivity.finish();
            Config.callActivity = null;
        }
        ScreenUtil.setScreen(this);
        setFinishOnTouchOutside(false);
        initViews();
        if (TextUtils.isEmpty(SpUtils.getString(LostActivity.this, "silent_time"))) {
            //没静默(没在静默时间段)
            MusicUtil.startCall(this);
        }
        Button sureBtn = (Button) findViewById(R.id.sureBtn);
        sureBtn.setOnClickListener(this);
    }

    public void initViews() {
        LinearLayout lost_ll = (LinearLayout) findViewById(R.id.lost_ll);
        for (int i = 0; i < Config.lostNameList.size(); i++) {
            View item = LayoutInflater.from(LostActivity.this).inflate(R.layout.lost_item, null);
            lost_ll.addView(item);
            View child = lost_ll.getChildAt(i);
            TextView textView = (TextView) child.findViewById(R.id.item_tv);
            textView.setText(Config.lostNameList.get(i));
        }
    }

    @Override
    public void onBackPressed() {
        Config.lostActivity = null;
        MusicUtil.stop();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sureBtn:
                Config.lostActivity = null;
                MusicUtil.stop();
                finish();
        }
    }
}
