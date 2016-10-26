package com.ifenglian.rocklet.ui.activity.set;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.SpUtils;

/**
 * 设置页面
 */
public class SetActivity extends Activity {

    private SelectMusicPopupWindow menuWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ImageView back_image = (ImageView) findViewById(R.id.back_image);
        RelativeLayout select_music_rl = (RelativeLayout) findViewById(R.id.select_music_rl);
        RelativeLayout about = (RelativeLayout) findViewById(R.id.about);
        //返回
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
            }
        });
        //选择铃声
        select_music_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow = new SelectMusicPopupWindow(SetActivity.this);
                menuWindow.showAtLocation(findViewById(R.id.set), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
        //关于我们
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SetActivity.this, AboutActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        menuWindow.dismiss();
        try {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                SpUtils.setString(this, "uri", uri.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (menuWindow != null) {
                menuWindow.dismiss();
                menuWindow = null;
            } else {
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
