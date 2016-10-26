package com.ifenglian.module.update;

import android.util.Log;

public class Constants {
	private static final boolean DEBUG = true;
	public static final String PERFRENCE_NAME = "com.max.module.update";
	public final static String KEY_FILE = "file";
	public static final String KEY_PCK = "pck";
	public static final long DUE_TM = 1000 * 60 * 60 * 2;
	public static final void LOG(String msg){
		if (DEBUG) {
			Log.i("Module.update", msg);
		}
	}
}
