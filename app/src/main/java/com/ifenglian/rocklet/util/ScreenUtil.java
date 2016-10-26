package com.ifenglian.rocklet.util;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Administrator on 2016/1/8.
 */
public class ScreenUtil {

    private static PowerManager.WakeLock mWakelock;

    /**
     * 在屏幕休眠时唤亮屏幕
     */
    public static boolean setScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screen = pm.isScreenOn();
        if (!screen) {
            mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.FULL_WAKE_LOCK, "SimpleTimer");
            mWakelock.acquire();
        }
        return screen;
    }

    public static void closeScreen(){
        if(mWakelock!=null){
            mWakelock.release();
            mWakelock=null;
        }
    }
}
