package com.ifenglian.rocklet.ui.activity.set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.SpUtils;


public class SelectMusicPopupWindow extends PopupWindow implements View.OnClickListener {

    private MediaPlayer mediaPlayer;
    private RadioButton radio1, radio2, radio3;
    private Activity activity;

    public SelectMusicPopupWindow(final Activity activity) {
        this.activity = activity;
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.select_music_popup_window, null);
        this.setContentView(mMenuView);
        this.setWidth(LinearLayout.LayoutParams.FILL_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);
        TextView text1 = (TextView) mMenuView.findViewById(R.id.text1);
        text1.setText(R.string.ringtoneone);
        TextView text2 = (TextView) mMenuView.findViewById(R.id.text2);
        text2.setText(R.string.ringtonetwo);
        TextView text3 = (TextView) mMenuView.findViewById(R.id.text3);
        text3.setText(R.string.ringtonethree);
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
        RelativeLayout rl_1 = (RelativeLayout) mMenuView.findViewById(R.id.rl_1);
        RelativeLayout rl_2 = (RelativeLayout) mMenuView.findViewById(R.id.rl_2);
        RelativeLayout rl_3 = (RelativeLayout) mMenuView.findViewById(R.id.rl_3);
        radio1 = (RadioButton) mMenuView.findViewById(R.id.radio1);
        radio2 = (RadioButton) mMenuView.findViewById(R.id.radio2);
        radio3 = (RadioButton) mMenuView.findViewById(R.id.radio3);
        rl_1.setOnClickListener(this);
        rl_2.setOnClickListener(this);
        rl_3.setOnClickListener(this);
        radio1.setOnClickListener(this);
        radio2.setOnClickListener(this);
        radio3.setOnClickListener(this);
        radio1.setChecked(false);
        radio2.setChecked(false);
        radio3.setChecked(false);
        String uri = SpUtils.getString(activity, "uri");
        if (TextUtils.isEmpty(uri)) {
            radio1.setChecked(true);
        }
        if (uri.equals("a")) {
            radio1.setChecked(true);
        } else if (uri.equals("b")) {
            radio2.setChecked(true);
        } else if (uri.equals("c")) {
            radio3.setChecked(true);
        } else {
            radio1.setChecked(true);
        }
        RelativeLayout more = (RelativeLayout) mMenuView.findViewById(R.id.more);
        Button cancel = (Button) mMenuView.findViewById(R.id.time_cancel);
        //取消
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                dismiss();
            }
        });
        Button sure = (Button) mMenuView.findViewById(R.id.time_sure);
        //确定
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                if (radio1.isChecked()) {
                    SpUtils.setString(activity, "uri", "a");
                }
                if (radio2.isChecked()) {
                    SpUtils.setString(activity, "uri", "b");
                }
                if (radio3.isChecked()) {
                    SpUtils.setString(activity, "uri", "c");
                }
                dismiss();
            }
        });
        //更多
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                Intent intent = new Intent();
                intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.setMusic);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                activity.startActivityForResult(intent, 0);
            }
        });
    }

    private void start(Activity activity, int resource) {
        stop();
        mediaPlayer = MediaPlayer.create(activity, resource);
        mediaPlayer.setLooping(false);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onClick(View v) {
        radio1.setChecked(false);
        radio2.setChecked(false);
        radio3.setChecked(false);
        if (v.getId() == R.id.rl_1 || v.getId() == R.id.radio1) {
            radio1.setChecked(true);
            start(activity, R.raw.a);
        } else if (v.getId() == R.id.rl_2 || v.getId() == R.id.radio2) {
            radio2.setChecked(true);
            start(activity, R.raw.b);
        } else if (v.getId() == R.id.rl_3 || v.getId() == R.id.radio3) {
            radio3.setChecked(true);
            start(activity, R.raw.c);
        }
    }
}
