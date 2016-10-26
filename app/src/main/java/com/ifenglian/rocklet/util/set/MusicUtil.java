package com.ifenglian.rocklet.util.set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.util.ScreenUtil;
import com.ifenglian.rocklet.util.SpUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/1/11.
 */
public class MusicUtil {

    private static Uri uri;
    private static MediaPlayer mediaPlayer;
    private static TipHelper helper;
    private static MyCount mc;

    public static void startCall(Activity activity) {
        stop();
        helper = new TipHelper();
        helper.Vibrate(activity, new long[]{1000, 500, 1000, 500}, true);
        mc = new MyCount(15000, 1000);
        mc.start();
        String uriStr = SpUtils.getString(activity, "uri");
        if (TextUtils.isEmpty(uriStr)) {
            mediaPlayer = MediaPlayer.create(activity, R.raw.a);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } else {
            uri = Uri.parse(uriStr);
        }
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (uri != null) {
            if (uriStr.equals("a") || uriStr.equals("b") || uriStr.equals("c")) {
                switch (uriStr) {
                    case "a":
                        mediaPlayer = MediaPlayer.create(activity, R.raw.a);
                        break;
                    case "b":
                        mediaPlayer = MediaPlayer.create(activity, R.raw.b);
                        break;
                    case "c":
                        mediaPlayer = MediaPlayer.create(activity, R.raw.c);
                        break;
                    default:
                        break;
                }
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                } else {
                    mediaPlayer.reset();
                }
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                }
                try {
                    mediaPlayer.setDataSource(activity, uri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void stop() {
        try {
            if (helper != null) {
                helper.close();
                helper = null;
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (mc != null) {
                mc.cancel();
            }
            ScreenUtil.closeScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            stop();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }
}
