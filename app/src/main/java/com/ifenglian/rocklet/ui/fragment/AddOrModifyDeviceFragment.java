package com.ifenglian.rocklet.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.util.BluetoothDeviceManage;
import com.ifenglian.rocklet.db.BluetoothDeviceDB;
import com.ifenglian.rocklet.util.APPUtil;
import com.ifenglian.rocklet.util.Config;

import java.io.File;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;


public class AddOrModifyDeviceFragment extends Fragment implements View.OnClickListener {

    private static final String MAC = "mac";
    private static final String DEVICE = "device";
    private Handler mHandler = new Handler();
    private BluetoothDeviceManage deviceManage;
    private Device device;
    private Button add_device_add_btn;
    private ImageView add_device_fdq_img, add_device_wallet_img, add_device_key_img, add_device_no_select_custom, add_device_card_img,
            add_device_box_img, add_device_select_custom;
    private EditText add_device_name_edt;
    private TextView parentTitleText, add_device_tip;
    private Bitmap bitmap;
    private String deviceName;
    private static final int IMAGE_REQUEST_CROP = 3;
    private static final int REQUEST_IMAGE = 4;
    private boolean isAddNewDevice;
    private boolean isClickBtn;

    public static AddOrModifyDeviceFragment newInstance(String mac, Device device) {
        AddOrModifyDeviceFragment fragment = new AddOrModifyDeviceFragment();
        Bundle args = new Bundle();
        args.putString(MAC, mac);
        args.putSerializable(DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    public AddOrModifyDeviceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mac = getArguments().getString(MAC);
            if (TextUtils.isEmpty(mac)) {
                isAddNewDevice = true;
                device = (Device) getArguments().getSerializable(DEVICE);
            } else {
                isAddNewDevice = false;
                device = BluetoothDeviceDB.getInstance(getActivity()).getDevByMac(mac);
                if (device == null) {
                    getActivity().finish();
                }
                deviceName = device.getName();
                bitmap = device.getHeadImage();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_device, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        //添加新设备
        if (isAddNewDevice) {
            parentTitleText.setText(R.string.add);
            add_device_tip.setText(R.string.add_device_notify_for_new_device);
            add_device_add_btn.setText(R.string.add_device_fragment_text_add_device);
            device = (Device) getArguments().getSerializable(DEVICE);
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.selected_fdq);
            add_device_fdq_img.setImageBitmap(bitmap);
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_fdq);
            add_device_name_edt.setText(R.string.add_device_fragment_text_fdq);
        } else { //修改设备信息
            parentTitleText.setText(R.string.add_device_activity_title_modify);
            add_device_tip.setText(R.string.add_device_notify_for_old_device);
            add_device_add_btn.setText(R.string.add_device_fragment_text_modify_device);
            initDeviceImage(bitmap);
            add_device_name_edt.setText(deviceName);
        }
        registerListener();
    }

    protected void initView() {
        parentTitleText = (TextView) getActivity().findViewById(R.id.add_device_title);
        add_device_tip = (TextView) getActivity().findViewById(R.id.add_device_tip);
        add_device_fdq_img = (ImageView) getActivity().findViewById(R.id.add_device_fdq_img);
        add_device_wallet_img = (ImageView) getActivity().findViewById(R.id.add_device_wallet_img);
        add_device_key_img = (ImageView) getActivity().findViewById(R.id.add_device_key_img);
        add_device_no_select_custom = (ImageView) getActivity().findViewById(R.id.add_device_no_select_custom);
        add_device_card_img = (ImageView) getActivity().findViewById(R.id.add_device_card_img);
        add_device_select_custom = (ImageView) getActivity().findViewById(R.id.add_device_select_custom);
        add_device_box_img = (ImageView) getActivity().findViewById(R.id.add_device_box_img);
        add_device_name_edt = (EditText) getActivity().findViewById(R.id.add_device_name_edt);
        add_device_add_btn = (Button) getActivity().findViewById(R.id.add_device_add_btn);

        add_device_name_edt.addTextChangedListener(new TextWatcher() {

            private CharSequence temp;
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                add_device_name_edt.setSelection(add_device_name_edt.getText().length());
                editStart = add_device_name_edt.getSelectionStart();
                editEnd = add_device_name_edt.getSelectionEnd();
                if (temp.length() > 10) {
                    //你输入的字数已经超过了限制
                    Toast.makeText(getActivity(),
                            R.string.big, Toast.LENGTH_SHORT)
                            .show();
                    s.delete(editStart - 1, editEnd);
                    add_device_name_edt.setText(s);
                    add_device_name_edt.setSelection(editStart);
                }
            }
        });
        TextView config_hidden = (TextView) getActivity().findViewById(R.id.config_hidden);
        config_hidden.requestFocus();
    }

    private void registerListener() {
        add_device_fdq_img.setOnClickListener(this);
        add_device_wallet_img.setOnClickListener(this);
        add_device_key_img.setOnClickListener(this);
        add_device_no_select_custom.setOnClickListener(this);
        add_device_card_img.setOnClickListener(this);
        add_device_box_img.setOnClickListener(this);
        add_device_add_btn.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_device_fdq_img:
                add_device_fdq_img.setImageResource(R.drawable.selected_fdq);
                add_device_wallet_img.setImageResource(R.drawable.default_wallet);
                add_device_key_img.setImageResource(R.drawable.default_key);
                add_device_no_select_custom.setImageResource(R.drawable.default_custom);
                add_device_card_img.setImageResource(R.drawable.default_work_card);
                add_device_box_img.setImageResource(R.drawable.default_box);
                isShowCustomImage(false);
                add_device_name_edt.setText(R.string.add_device_fragment_text_fdq);
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_fdq);
                break;
            case R.id.add_device_wallet_img:
                add_device_fdq_img.setImageResource(R.drawable.default_fdq);
                add_device_wallet_img.setImageResource(R.drawable.selected_wallet);
                add_device_key_img.setImageResource(R.drawable.default_key);
                add_device_no_select_custom.setImageResource(R.drawable.default_custom);
                add_device_card_img.setImageResource(R.drawable.default_work_card);
                add_device_box_img.setImageResource(R.drawable.default_box);
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_wallet);
                add_device_name_edt.setText(R.string.add_device_fragment_text_wallet);
                isShowCustomImage(false);
                break;
            case R.id.add_device_key_img:
                add_device_fdq_img.setImageResource(R.drawable.default_fdq);
                add_device_wallet_img.setImageResource(R.drawable.default_wallet);
                add_device_key_img.setImageResource(R.drawable.selected_key);
                add_device_no_select_custom.setImageResource(R.drawable.default_custom);
                add_device_card_img.setImageResource(R.drawable.default_work_card);
                add_device_box_img.setImageResource(R.drawable.default_box);
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_key);
                add_device_name_edt.setText(R.string.add_device_fragment_text_key);
                isShowCustomImage(false);
                break;
            case R.id.add_device_no_select_custom:
                //自定义头像
                add_device_fdq_img.setImageResource(R.drawable.default_fdq);
                add_device_wallet_img.setImageResource(R.drawable.default_wallet);
                add_device_key_img.setImageResource(R.drawable.default_key);
                add_device_card_img.setImageResource(R.drawable.default_work_card);
                add_device_box_img.setImageResource(R.drawable.default_box);
                selectorPic();
                add_device_name_edt.setText(null);
                break;
            case R.id.add_device_card_img:
                add_device_fdq_img.setImageResource(R.drawable.default_fdq);
                add_device_wallet_img.setImageResource(R.drawable.default_wallet);
                add_device_key_img.setImageResource(R.drawable.default_key);
                add_device_no_select_custom.setImageResource(R.drawable.default_custom);
                add_device_card_img.setImageResource(R.drawable.selected_work_card);
                add_device_box_img.setImageResource(R.drawable.default_box);
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_work_card);
                add_device_name_edt.setText(R.string.add_device_fragment_text_work_card);
                isShowCustomImage(false);
                break;
            case R.id.add_device_box_img:
                add_device_fdq_img.setImageResource(R.drawable.default_fdq);
                add_device_wallet_img.setImageResource(R.drawable.default_wallet);
                add_device_key_img.setImageResource(R.drawable.default_key);
                add_device_no_select_custom.setImageResource(R.drawable.default_custom);
                add_device_card_img.setImageResource(R.drawable.default_work_card);
                add_device_box_img.setImageResource(R.drawable.selected_box);
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_box);
                add_device_name_edt.setText(R.string.add_device_fragment_text_box);
                isShowCustomImage(false);
                break;
            case R.id.add_device_add_btn:
                if (isAddNewDevice) {
                    //添加新设备
                    deviceName = add_device_name_edt.getText().toString().trim();
                    if (TextUtils.isEmpty(deviceName)) {
                        //请先设置设备名称
                        Toast.makeText(getActivity(), getString(R.string.set_device_name), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        APPUtil.showLoading(getActivity());
                        addNewBluetoothDevice();
                    }
                } else {
                    //修改设备信息
                    deviceName = add_device_name_edt.getText().toString().trim();
                    if (TextUtils.isEmpty(deviceName)) {
                        Toast.makeText(getActivity(), getString(R.string.set_device_name), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isClickBtn) {
                        APPUtil.showLoading(getActivity());
                        int id = Integer.parseInt(device.getId());
                        int result = BluetoothDeviceDB.getInstance(getContext()).UpdateNameAndIconById(id, deviceName, bitmap);
                        if (result == 1) {
                            isClickBtn = true;
                            BluetoothDeviceDB.getInstance(getContext()).CloseDb();
                            APPUtil.hideLoading();
                            getActivity().finish();
                        } else {
                            APPUtil.hideLoading();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void selectorPic() {
        Intent intent = new Intent(getContext(), MultiImageSelectorActivity.class);
        //显示相机
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        //单选
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void isShowCustomImage(boolean enable) {
        if (enable) {
            add_device_select_custom.setVisibility(View.VISIBLE);
        } else {
            add_device_select_custom.setVisibility(View.GONE);
        }
    }

    /**
     * 添加新设备
     */
    private void addNewBluetoothDevice() {
        deviceManage = new BluetoothDeviceManage(getContext(), true);
        deviceManage.init();
        deviceManage.setIsConnectSuccess(new BluetoothDeviceManage.isConnectSuccess() {

            public void addSuccess() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDeviceDB deviceDataBase = BluetoothDeviceDB.getInstance(getContext());
                        if (!deviceDataBase.isTableExits()) {
                            deviceDataBase.CreateTable();
                        }
                        deviceDataBase.addDev(deviceName, device.getMac(), bitmap, deviceManage.getBattLevel() + "", deviceManage.getFirmwareVersion(), "1");
                        deviceDataBase.CloseDb();
                        device.setState(1);
                        Config.devices.add(device);
                        Config.deviceManages.add(deviceManage);
                        APPUtil.hideLoading();
                        getActivity().finish();
                    }
                });
            }

            public void addFail() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //添加失败，请重新添加!
                        Toast.makeText(getContext(), R.string.add_device_fragment_fail_prompt, Toast.LENGTH_SHORT).show();
                        APPUtil.hideLoading();
                    }
                });
                deviceManage.connectDevice(device.getMac());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CROP) {
                if (data != null) {
                    Bitmap image = data.getParcelableExtra("data");
                    bitmap = new APPUtil().toRoundBitmap(image);
                    add_device_no_select_custom.setImageBitmap(bitmap);
                    isShowCustomImage(true);
                }
            } else if (requestCode == REQUEST_IMAGE) {
                // 获取返回的图片
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                String picPath = "file://" + path.get(0);
                Uri imageUri = Uri.parse(picPath);
                crop(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", add_device_no_select_custom.getWidth());
        intent.putExtra("outputY", add_device_no_select_custom.getHeight());
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, IMAGE_REQUEST_CROP);
    }


    /**
     * 初始化头像
     *
     * @param bt
     */
    private void initDeviceImage(Bitmap bt) {
        Activity mActivity = getActivity();
        if (APPUtil.isEqual(mActivity, bt, ((BitmapDrawable) add_device_fdq_img.getDrawable()).getBitmap())) {
            add_device_fdq_img.setImageResource(R.drawable.selected_fdq);
        } else if (APPUtil.isEqual(mActivity, bt, ((BitmapDrawable) add_device_wallet_img.getDrawable()).getBitmap())) {
            add_device_wallet_img.setImageResource(R.drawable.selected_wallet);
        } else if (APPUtil.isEqual(mActivity, bt, ((BitmapDrawable) add_device_key_img.getDrawable()).getBitmap())) {
            add_device_key_img.setImageResource(R.drawable.selected_key);
        } else if (APPUtil.isEqual(mActivity, bt, ((BitmapDrawable) add_device_card_img.getDrawable()).getBitmap())) {
            add_device_card_img.setImageResource(R.drawable.selected_work_card);
        } else if (APPUtil.isEqual(mActivity, bt, ((BitmapDrawable) add_device_box_img.getDrawable()).getBitmap())) {
            add_device_box_img.setImageResource(R.drawable.selected_box);
        } else {
            add_device_no_select_custom.setImageBitmap(bt);
            isShowCustomImage(true);
        }

    }
}
