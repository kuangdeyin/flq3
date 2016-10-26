package com.ifenglian.rocklet.util;

import android.util.Log;

public class PhoneOnlyId {
	private static final String TAG = "PhoneOnlyId";
	private static final int PHONE_ID_LENGTH = 16;
	private byte[] idBytes;
	private String idStr;

	public PhoneOnlyId(byte[] bytes) {
		if (bytes.length != PHONE_ID_LENGTH / 2) {
			Log.e(TAG, "wrong device id length!");
			return;
		}
		idBytes = bytes.clone();
		idStr = byte2String(idBytes);
	}

	public PhoneOnlyId(String phoneId) {
		if (phoneId.length() != PHONE_ID_LENGTH) {
			Log.e(TAG, "wrong device id length!");
			return;
		}
		idBytes = string2Byte(phoneId);
	}

	public byte[] getBytes() {
		return idBytes;
	}

	@Override
	public String toString() {
		return idStr;
	}

	public boolean isBlankId() {
		if (idStr == null)
			return false;
		return idStr.equals("FFFFFFFFFFFFFFFF");
	}

	private String byte2String(byte[] bytes) {
		if (bytes == null)
			return "";
		StringBuilder md5Str = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (Integer.toHexString(0xFF & bytes[i]).length() == 1)
				md5Str.append("0").append(
						Integer.toHexString(0xFF & bytes[i]).toUpperCase());
			else
				md5Str.append(Integer.toHexString(0xFF & bytes[i])
						.toUpperCase());
		}
		return md5Str.toString();
	}

	private byte[] string2Byte(String str) {
		int len = (str.length() / 2);
		byte[] result = new byte[len];
		char[] achar = str.toCharArray();
		try {
			for (int i = 0; i < len; i++) {
				int pos = i * 2;
				result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
			}
		} catch (NumberFormatException ne) {
			ne.printStackTrace();
			return null;
		}
		return result;
	}

	private byte toByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public boolean equals(PhoneOnlyId newId) {
		return idStr.equals(newId.toString());
	}
}
