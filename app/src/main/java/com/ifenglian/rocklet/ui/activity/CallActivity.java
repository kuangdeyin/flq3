package com.ifenglian.rocklet.ui.activity;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.Config;
import com.ifenglian.rocklet.util.SpUtils;
import com.ifenglian.rocklet.util.set.MusicUtil;

/**
 * 呼叫弹出页面
 */
public class CallActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_call);
        getWindow().setBackgroundDrawable(new BitmapDrawable());
        setFinishOnTouchOutside(false);
        if (Config.lostActivity != null) {
            Config.lostActivity.finish();
            Config.lostActivity = null;
        }
        if(Config.timeActivity!=null){
            Config.timeActivity.finish();
            Config.timeActivity=null;
        }
        if(Config.callActivity !=null){
            Config.callActivity.finish();
            Config.callActivity =null;
        }
        String name = getIntent().getStringExtra("name");
        TextView nameText = (TextView) findViewById(R.id.name);
        if (!TextUtils.isEmpty(name)) {
            nameText.setText(name);
        }
        Config.callActivity = this;

        TextView text = (TextView) findViewById(R.id.text);
        text.setText(getString(R.string.you));
        TextView tv = (TextView) findViewById(R.id.tv);
        //正在呼叫手机
        tv.setText(getString(R.string.calling));
        if (TextUtils.isEmpty(SpUtils.getString(CallActivity.this,"silent_time"))) {
            //没静默(没在静默时间段)
            MusicUtil.startCall(this);
        }
        Button sureBtn = (Button) findViewById(R.id.sureBtn);
        //我知道了
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.call_mac = null;
                Config.callActivity=null;
                MusicUtil.stop();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        MusicUtil.stop();
        Config.call_mac = null;
        Config.callActivity=null;
        finish();
    }
}
