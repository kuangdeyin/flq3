package com.ifenglian.rocklet.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.ui.activity.OpenBluActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by licy on 2016/1/8.
 */
public class APPUtil {

    public static final String KEY_ONLY_ID = "only_id";
    private static PhoneOnlyId onlyId = null;
    private static Context mContext;
    private static ProgressDialog mProgressDialog;

    public static void showLoading(Context context) {
        if (context == null)
            return;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.progress);
    }

    /**
     * 关闭Loading
     */
    public static void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public static PhoneOnlyId getOnlyId(Context context) {
        if (onlyId != null)
            return onlyId;
        String onlyIdStr = SpUtils.getString(context, KEY_ONLY_ID);
        if (onlyIdStr.isEmpty()) {
            onlyId = getDeviceOnlyId(context);
            if (onlyId != null)
                SpUtils.setString(context, KEY_ONLY_ID, onlyId.toString());
            else
                SpUtils.setString(context, KEY_ONLY_ID, "");
        } else {
            onlyId = new PhoneOnlyId(onlyIdStr);
        }
        return onlyId;
    }

    private static PhoneOnlyId getDeviceOnlyId(Context context) {
        mContext = context;
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder idStr = new StringBuilder();
        String sn = Build.SERIAL;
        if (sn != null && !sn.isEmpty()) {
            idStr.append("sn");
            idStr.append(sn);
        }
        if (mTelephonyManager.getDeviceId() != null && !mTelephonyManager.getDeviceId().isEmpty()) {
            idStr.append("imei");
            idStr.append(mTelephonyManager.getDeviceId());
        }
        if (idStr.length() == 0) {
            idStr.append("androidId");
            idStr.append(getAndroidID());
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(idStr.toString().getBytes());
            byte[] md5bytes16 = md5.digest();
            byte[] md5bytes8 = new byte[8];
            for (int i = 0; i < 8; i++) {
                md5bytes8[i] = md5bytes16[i + 4];
            }
            return new PhoneOnlyId(md5bytes8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAndroidID() {
        return Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * 检测蓝牙是否打开未打开则就去打开蓝牙
     */
    public static void checkBluetoothOpen(Activity activity) {
        /**
         * final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
         bluetoothAdapter = bluetoothManager.getAdapter();
         //不支持蓝牙
         if (bluetoothAdapter == null) {
         Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported,
         Toast.LENGTH_SHORT).show();
         searchFragmentResult(0, null);
         return;
         }

         //判断手机是否支持ble设备 ，不支持就退出关闭程序
         if (!getPackageManager().hasSystemFeature(
         PackageManager.FEATURE_BLUETOOTH_LE)) {
         Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT)
         .show();
         finish();
         }
         */
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //设备不支持蓝牙
            Toast.makeText(activity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //蓝牙未打开
            Config.BluetoothOpen = false;
            Intent intent = new Intent(activity, OpenBluActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } else {
            Config.BluetoothOpen = true;
        }
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);// 设置画笔无锯齿
        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        // 以下有两种方法画圆,drawRoundRect和drawCircle
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        // canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }

    public static boolean isEqual(Activity activity, Bitmap expectedBitmap, Bitmap actualBitmap) {
        int nonMatchingPixels = 0;
        int allowedMaxNonMatchPixels = 10;
        if (expectedBitmap == null || actualBitmap == null || activity == null) {
            return false;
        }
        int[] expectedBmpPixels = new int[expectedBitmap.getWidth() * expectedBitmap.getHeight()];
        expectedBitmap.getPixels(expectedBmpPixels, 0, expectedBitmap.getWidth(), 0, 0, expectedBitmap.getWidth(), expectedBitmap.getHeight());
        int[] actualBmpPixels = new int[actualBitmap.getWidth() * actualBitmap.getHeight()];
        actualBitmap.getPixels(actualBmpPixels, 0, actualBitmap.getWidth(), 0, 0, actualBitmap.getWidth(), actualBitmap.getHeight());
        if (expectedBmpPixels.length != actualBmpPixels.length) {
            return false;
        }
        for (int i = 0; i < expectedBmpPixels.length; i++) {
            if (expectedBmpPixels[i] != actualBmpPixels[i]) {
                nonMatchingPixels++;
            }
        }
        if (nonMatchingPixels > allowedMaxNonMatchPixels) {
            return false;
        }
        return true;
    }


    public static void setTime(Context context, String silent_time) {
        SpUtils.setString(context, "silent_time", silent_time);
    }
}
