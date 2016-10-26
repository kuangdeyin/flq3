package com.ifenglian.rocklet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.Config;

/**
 * 打开蓝牙提示框
 */
public class OpenBluActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_open);
        getWindow().setBackgroundDrawable(new BitmapDrawable());
        Config.OpenBleActivity =this;
        setFinishOnTouchOutside(false);
        TextView open_text=(TextView)findViewById(R.id.open_text);
        open_text.setText(getString(R.string.error_bluetooth_not_enabled));
        Button open_sureBtn=(Button)findViewById(R.id.open_sureBtn);
        open_sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.OpenBleActivity =null;
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Config.OpenBleActivity = null;
    }
}
