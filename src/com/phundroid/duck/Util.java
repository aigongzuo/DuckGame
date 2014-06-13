package com.phundroid.duck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class Util {
//	public static String serviceURL = "http://192.168.18.132:8080/LBSService/";
	public static String serviceURL = "http://lbsservice.jd-app.com/";

	public static String getPhoneNumber(Context context) {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	public static String getSingleID(Context context) {
		String DEVICE_ID = "";
		try {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			DEVICE_ID = tm.getDeviceId();
			if (DEVICE_ID == null) {
				DEVICE_ID = tm.getSimSerialNumber();
				if (DEVICE_ID == null) {
					DEVICE_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
					if (DEVICE_ID == null)
						DEVICE_ID = android.os.Build.SERIAL;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (DEVICE_ID == null)
			DEVICE_ID = "";
		return DEVICE_ID;
	}

	public static int getNOID(String str) {
		try {
			Pattern p = Pattern.compile("<NOID>(.*?)</NOID>");
			Matcher m = p.matcher(str);
			if (m.find()) {
				return Integer.parseInt(m.group(1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public static String getSTRNOID(String str) {
		try {
			Pattern p = Pattern.compile("<NOID>(.*?)</NOID>");
			Matcher m = p.matcher(str);
			if (m.find()) {
				return m.group(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "0";
	}

	public static String getTitle(String str) {
		Pattern p = Pattern.compile("<Title>(.*?)</Title>");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	public static String getContext(String str) {
		Pattern p = Pattern.compile("<Context>(.*?)</Context>");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	public static String getURL(String str) {
		Pattern p = Pattern.compile("<URL>(.*?)</URL>");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}
}
