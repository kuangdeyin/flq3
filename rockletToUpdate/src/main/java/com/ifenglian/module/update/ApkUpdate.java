package com.ifenglian.module.update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;

public class ApkUpdate {
	private Context mContext;
	private String appName = "";
	private SharedPreferences mPreferences;

	private String mPackageName;
	private AlertDialog mDialog;

	public ApkUpdate(Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(Constants.PERFRENCE_NAME,
				Context.MODE_PRIVATE);
	}

	public void update() {
		update("/mnt/sdcard/flq");
	}

	public void update(String savePath) {
		PackageManager packageManager = mContext.getPackageManager();
		mPackageName = mContext.getPackageName();
		PackageInfo packInfo = null;
		ApplicationInfo appInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(mPackageName, 0);
			appInfo = packageManager.getApplicationInfo(mPackageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packInfo != null && appInfo != null) {
			long date = mPreferences.getLong(mPackageName, 0);
			long now = System.currentTimeMillis();
			if (date != 0 && (now - date) < Constants.DUE_TM) {
				return;
			}
			int version = packInfo.versionCode;
			appName = (String) packageManager.getApplicationLabel(appInfo);
			new ApkLoader().execute(mPackageName, "" + version, savePath);
		}
	}

	class ApkLoader extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			return Request.requestApk(params[0], params[1], params[2]);
		}

		@Override
		protected void onPostExecute(final String path) {
			super.onPostExecute(path);//   /mnt/sdcard/flq/com.ifenglian.rocklet.1.0.541.apk
			Constants.LOG("================" + path);
			if (path != null) {
				if (mDialog == null) {
					mDialog = new AlertDialog.Builder(mContext).create();
					mDialog.setTitle(R.string.title_tip);
					mDialog.setMessage(mContext.getString(
							R.string.msg_has_new_version, appName));
					mDialog.setCancelable(false);
					mDialog.setButton(DialogInterface.BUTTON_POSITIVE,
							mContext.getString(R.string.btn_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setDataAndType(Uri.fromFile(new File(path)),
											"application/vnd.android.package-archive");
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									mContext.startActivity(intent);

									Editor editor = mPreferences.edit();
									editor.putLong(mPackageName, 0);
									editor.commit();
									dialog.dismiss();
								}
							});
					mDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
							mContext.getString(R.string.btn_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Editor editor = mPreferences.edit();
									editor.putLong(mPackageName,
											System.currentTimeMillis());
									editor.commit();
									dialog.dismiss();
								}
							});
				}
				if (!mDialog.isShowing()) {
					try {
						mDialog.show();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
