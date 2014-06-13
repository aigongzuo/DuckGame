package com.phundroid.duck;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
	Context context;

	public SharedPreferencesUtil(Context context) {
		this.context = context;
	}

	public void SetUpdateTime(int time) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("TIME", time);
		editor.commit();
	}

	public int getUpdateTime() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getInt("TIME", 10 * 1000);
	}

	public void SetStep(int step) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("STEP", step);
		editor.commit();
	}

	public int GetStep() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getInt("STEP", 0);
	}

	public void SetSensor(int time) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("SENSOR", time);
		editor.commit();
	}

	public int GetSensor() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getInt("SENSOR", 300);
	}

	public void SetTimeInterval(int time) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("TIMEINTERVAL", time);
		editor.commit();
	}

	public int GetTimeInterval() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getInt("TIMEINTERVAL", 200);
	}

	public void SetisShark(boolean shark) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("IsShark", shark);
		editor.commit();
	}

	public boolean GetisShark() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getBoolean("IsShark", false);
	}

	public void SetPOI(String POI) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("POI", POI);
		editor.commit();
	}

	public String GetPOI() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getString("POI", "大厦");
	}
	public void SetNOID(String NOID) {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("NOID", NOID);
		editor.commit();
	}
	
	public String GetNOID() {
		SharedPreferences sp = context.getSharedPreferences("Data", context.MODE_PRIVATE);
		return sp.getString("NOID", "0");
	}

}
