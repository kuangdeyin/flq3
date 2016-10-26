package com.ifenglian.module.update;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

@SuppressLint("SdCardPath")
public class Request {
	private static String HOST = "http://ottservers.netcoretec.com:60136";
	private static final String IMG_URL = "/luncher_pic_path.php";
	private static final String APK_URL = "/apk_upgrade.php";
	static{
		initHost();
	}
	
	private static void initHost(){
		String host = System.getProperty("ro.apk.update.server");
		Constants.LOG("read host:" + host);
		if (!(host == null) && !host.isEmpty()) {
			HOST = "http://" + host;
		}
	}
	public static String requestImg(String type){
		String url = HOST + IMG_URL + "?type=" + type;
		String content = HttpRequest.httpUrlConnection(url,null,"GET");
		Constants.LOG("requestImg:" + content);
		try {
			if (content == null || "NULL".equals(content)) {
				return null;
			}
			JSONObject object = new JSONObject(content);
			String path = object.getString("picpath");
			String md5Server = object.getString("md5");
			
			int result = HttpDownLoader.loadRmoteFile(HOST + "/" + path,"/mnt/sdcard/launcherData", "wallpager.jpg", true);
			switch (result) {
			case -1:
				return null;
			case 0:
			case 1:
				try {
					String md5File = MD5Util.getFileMD5String(new File(
							"/mnt/sdcard/launcherData/wallpager.jpg"));
					Constants.LOG(md5File);
					if (md5Server == null || !md5Server.equals(md5File)) {
						return null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "/mnt/sdcard/launcherData/wallpager.jpg";
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String requestApk(String packageName,String versionCode,String path){
		String url = HOST + APK_URL + "?packageName=" + packageName + "&version=" + versionCode + "&model=BT5001";
		String savePath = "/mnt/sdcard/apkupgrad";
		if (path != null) {
			savePath = path;
		}
		Constants.LOG("request url:" + url);
		String content = HttpRequest.httpUrlConnection(url,null,"GET");
		Constants.LOG("requestApk:" + content);
		try {
			if (content == null || "NULL".equals(content)) {
				return null;
			}
			JSONObject object = new JSONObject(content);
			String apkname = object.getString("apkname");
			String newVersion = object.getString("versionname");
			String downloadpath = object.getString("downloadpath");
			String md5Server = object.getString("md5");
			String fileName = packageName + "." + newVersion + ".apk";
			int result = HttpDownLoader.loadRmoteFile(HOST + "/" + downloadpath,savePath, fileName, true);
			switch (result) {
			case -1:
				return null;
			case 0:
			case 1:
				Constants.LOG(md5Server);
				String md5File = MD5Util.getFileMD5String(new File(savePath + "/" + fileName));
				Constants.LOG(md5File);
				if (md5Server == null || !md5Server.equals(md5File)) {
					return null;
				}
				return savePath + "/" + fileName;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
