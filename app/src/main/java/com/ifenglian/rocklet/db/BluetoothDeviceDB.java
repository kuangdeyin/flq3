package com.ifenglian.rocklet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ifenglian.rocklet.bean.Device;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by licy on 2015/12/28.
 */
public class BluetoothDeviceDB {

    private static BluetoothDeviceDB instance;
    private SQLiteDatabase db;
    private final String tableName = "BluetoothDetailsTable";

    public static BluetoothDeviceDB getInstance(Context context) {
        if (null == instance) {
            instance = new BluetoothDeviceDB(context);
        }
        return instance;
    }

    /**
     * 创建打开数据库，已创建就打开数据库
     *
     * @param context
     */
    public BluetoothDeviceDB(Context context) {
        db = context.openOrCreateDatabase("bluetooth.db", Context.MODE_PRIVATE,
                null);
    }

    /**
     * 新建一个表
     */
    public void CreateTable() {
        String sql = "create table if not exists " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT,name " +
                "varchar,mac varchar,image BLOB,electricity varchar,edition varchar,state integer)";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.i("err", "create table failed");
        }
    }


    /**
     * 插入数据
     */
    public void addDev(String name, String mac, Bitmap portrait, String electricity, String edition, String state) {

        ContentValues values = new ContentValues();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        portrait.compress(Bitmap.CompressFormat.PNG, 100, os);
        values.put("name", name);
        values.put("mac", mac);
        values.put("image", os.toByteArray());//以字节形式保存
        values.put("electricity", electricity);
        values.put("edition", edition);
        values.put("state", state);//连接状态 0 未连接 ，1已连接
        try {
            db.insert(tableName, null, values);
        } catch (SQLException e) {
            Log.i("err", "insert failed");
        }
    }

    /**
     * 根据ID修改头像和名称数据
     */
    public int UpdateNameAndIconById(int id, String name, Bitmap headIcon) {
        int result = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            headIcon.compress(Bitmap.CompressFormat.PNG, 100, os);
            values.put("image", os.toByteArray());
            String whereClause = "id=?";
            String[] whereArgs = new String[]{String.valueOf(id)};
            result = db.update(tableName, values, whereClause, whereArgs);
        } catch (SQLException e) {
            Log.i("err", "update failed");
        }
        return result;
    }

    /**
     * 更新电量和版本号
     */
    public void UpdateEleAndEditionByMac(String mac, String electricity, String edition) {
        try {
            Device device = getDevByMac(mac);
            if (device != null) {
                ContentValues values = new ContentValues();
                values.put("electricity", electricity);
                values.put("edition", edition);
                String whereClause = "id=?";
                String[] whereArgs = new String[]{String.valueOf(device.getId())};
                db.update(tableName, values, whereClause, whereArgs);
            }
        } catch (SQLException e) {
            Log.i("err", "update failed");
        }
    }

    /**
     * 根据mac 地址查询数据Id
     */

    public Device getDevByMac(String mac) {
        Device device = null;
        String sql = "select * from " + tableName + " where mac='" + mac + "'";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            device = new Device();
            device.setId(cursor.getString(cursor.getColumnIndex("id")));
            device.setName(cursor.getString(cursor.getColumnIndex("name")));
            device.setMac(cursor.getString(cursor.getColumnIndex("mac")));
            byte[] image = cursor.getBlob(cursor.getColumnIndex("image"));
            device.setHeadImage(BitmapFactory.decodeByteArray(image, 0, image.length));
            device.setElectricity(cursor.getString(cursor.getColumnIndex("electricity")));
            device.setEdition(cursor.getString(cursor.getColumnIndex("edition")));
            device.setState(cursor.getInt(cursor.getColumnIndex("state")));
        }
        cursor.close();
        return device;
    }

    /**
     * 查询所有数据
     */
    public List<Device> getDevs() {
        List<Device> devices = new ArrayList<Device>();
        String sql = "select * from " + tableName;
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            Device device = new Device();
            String id = cursor.getString(cursor.getColumnIndex("id"));
            device.setId(id);
            device.setName(cursor.getString(cursor.getColumnIndex("name")));
            device.setMac(cursor.getString(cursor.getColumnIndex("mac")));
            byte[] in = cursor.getBlob(cursor.getColumnIndex("image"));
            device.setHeadImage(BitmapFactory.decodeByteArray(in, 0, in.length));
            device.setElectricity(cursor.getString(cursor.getColumnIndex("electricity")));
            device.setEdition(cursor.getString(cursor.getColumnIndex("edition")));
            device.setState(cursor.getInt(cursor.getColumnIndex("state")));
            devices.add(device);
        }
        cursor.close();
        return devices;
    }


    /**
     * 删除指定数据
     */
    public void DeleteDevById(int id) {
        String sql = "delete from " + tableName + " where id=" + id;
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.i("err", "delete failed");
        }
    }

    /**
     * 关闭数据库
     */
    public void CloseDb() {
        if (db != null) {
            instance = null;
            db.close();
        }
    }

    /**
     * 判断表是否存在
     *
     * @return
     */
    public boolean isTableExits() {
        boolean result = false;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName + "' ";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
            cursor.close();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }
}
