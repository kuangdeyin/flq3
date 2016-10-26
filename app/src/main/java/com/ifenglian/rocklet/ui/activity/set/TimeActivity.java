package com.ifenglian.rocklet.ui.activity.set;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.APPUtil;
import com.ifenglian.rocklet.util.Config;

/**
 * 设置静默时间弹出页面
 */
public class TimeActivity extends Activity {

    private String selected="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_time);
        getWindow().setBackgroundDrawable(new BitmapDrawable());
        Config.timeActivity=this;
        Button sureBtn=(Button)findViewById(R.id.sureBtn);
        final ImageView time1=(ImageView)findViewById(R.id.time1);
        final ImageView time2=(ImageView)findViewById(R.id.time2);
        final ImageView time3=(ImageView)findViewById(R.id.time3);
        final ImageView time4=(ImageView)findViewById(R.id.time4);
        time1.setBackgroundResource(R.drawable.check1);
        time2.setBackgroundResource(R.drawable.check2);
        time3.setBackgroundResource(R.drawable.check2);
        time4.setBackgroundResource(R.drawable.check2);
        selected=System.currentTimeMillis() + ","+10*60*1000;
        time1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time1.setBackgroundResource(R.drawable.check1);
                time2.setBackgroundResource(R.drawable.check2);
                time3.setBackgroundResource(R.drawable.check2);
                time4.setBackgroundResource(R.drawable.check2);
                selected=System.currentTimeMillis() + ","+10*60*1000;
            }
        });
        time2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time1.setBackgroundResource(R.drawable.check2);
                time2.setBackgroundResource(R.drawable.check1);
                time3.setBackgroundResource(R.drawable.check2);
                time4.setBackgroundResource(R.drawable.check2);
                selected=System.currentTimeMillis() + ","+30*60*1000;
            }
        });
        time3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time1.setBackgroundResource(R.drawable.check2);
                time2.setBackgroundResource(R.drawable.check2);
                time3.setBackgroundResource(R.drawable.check1);
                time4.setBackgroundResource(R.drawable.check2);
                selected=System.currentTimeMillis() + ","+60*60*1000;
            }
        });
        time4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time1.setBackgroundResource(R.drawable.check2);
                time2.setBackgroundResource(R.drawable.check2);
                time3.setBackgroundResource(R.drawable.check2);
                time4.setBackgroundResource(R.drawable.check1);
                selected=System.currentTimeMillis() + ","+180*60*1000;
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APPUtil.setTime(TimeActivity.this, selected);
                Config.timeActivity=null;
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Config.timeActivity=null;
        finish();
    }
}
