package com.ifenglian.rocklet.util.set;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

/**
 * Created by Administrator on 2015/12/17.
 * 震动
 */
public class TipHelper {

    private Vibrator vibrator;

    public void Vibrate(final Activity activity, long[] pattern, boolean isRepeat) {
        vibrator = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, isRepeat ? 1 : -1);
    }

    public void close() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator=null;
        }
    }
}
