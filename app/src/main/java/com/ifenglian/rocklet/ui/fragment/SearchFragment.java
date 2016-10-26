package com.ifenglian.rocklet.ui.fragment;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class SearchFragment extends Fragment {

    private int searchCount = 0;
    private boolean scanComplete = false;
    private Handler handler;
    private ImageView add_icon;
    private ImageView searchGestureImg;
    private ValueAnimator valueAnimator;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<Device> devices = new ArrayList<>();
    private static final long SCAN_PERIOD = 5000;
    private ResultListener mListener;
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final android.bluetooth.BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            //判断设备是不是已经被绑定过防止重复绑定
            BluetoothDeviceDB db = BluetoothDeviceDB.getInstance(getActivity());
            if (db.isTableExits()) {
                Device dev = db.getDevByMac(device.getAddress());
                if (dev != null) {
                    return;
                }
            }
            if (TextUtils.isEmpty(device.getName())) {
                if (device.getName().equals("SmarTag")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Device dev = new Device();
                            dev.setMac(device.getAddress());
                            dev.setRssi(rssi);
                            dev.setName(device.getName());
                            devices.add(dev);
                        }
                    });
                }
            }
        }
    };


    public SearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchGestureImg = (ImageView) getActivity().findViewById(R.id.search_gesture);
        add_icon = (ImageView) getActivity().findViewById(R.id.add_icon);
        valueAnimator = ValueAnimator.ofInt(1, 360);
        final PointF pointF = new PointF(0, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int x = (int) (pointF.x + 40 * Math.cos(value * 3.14 / 180));
                int y = (int) (pointF.y + 40 * Math.sin(value * 3.14 / 180));
                PointF pointF1 = new PointF(x, y);
                add_icon.setTranslationX(pointF1.x);
                add_icon.setTranslationY(pointF1.y);
            }
        });

        valueAnimator.setDuration(4000);
        valueAnimator.setStartDelay(300);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in_for_search);
        searchGestureImg.startAnimation(animation);

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        //不支持蓝牙
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            searchFragmentResult(0, null);
            return;
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //扫描周围设备
                scanLeDevice();
            }
        }, SCAN_PERIOD);
    }

    private void scanLeDevice() {
        if (searchCount > 2) {
            scanComplete = true;
            bluetoothAdapter.stopLeScan(leScanCallback);
            searchFragmentResult(0, null);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!scanComplete) {
                        bluetoothAdapter.stopLeScan(leScanCallback);
                        if (devices.isEmpty()) {
                            //没找到设备，重新查找，提示请将未绑定防丢器靠近手机!
                            scanLeDevice();
                            Toast.makeText(getActivity(), R.string.near, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            //按照rssi信号强度排序
                            Collections.sort(devices, new Comparator<Device>() {
                                @Override
                                public int compare(Device lhs, Device rhs) {
                                    if (lhs.getRssi() > rhs.getRssi())
                                        return 1;
                                    else
                                        return -1;
                                }
                            });
                            searchFragmentResult(1, devices.get(0));
                        }
                    }
                }
            }, SCAN_PERIOD);
            searchCount++;
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ResultListener) {
            mListener = (ResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler.removeCallbacksAndMessages(null);
        handler = null;
        mListener = null;
        if (valueAnimator != null)
            valueAnimator.end();
        if (searchGestureImg != null) {
            searchGestureImg.clearAnimation();
            searchGestureImg = null;
        }
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    private void searchFragmentResult(int resultCode, Device pDeviceBin) {
        if (mListener == null) {
            return;
        }
        mListener.searchResult(resultCode, pDeviceBin);
    }

    public interface ResultListener {

        void searchResult(int actionCode, Device pDeviceBin);

    }
}
