package com.phundroid.duck;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.widget.Toast;

public class ShakeListener implements SensorEventListener {

	private Vibrator mVibrator = null;// 振动器
	private Context mContext = null;
	private SensorManager sm = null;// 传感器管理器
	private Sensor mSensor = null;// 传感器

	private int update_time = 200;// 2次更新间隔时间
	private long lastUpdateTime;// 上次更新時間
	private int update_speed = 300;// 速度大于此速度才能更新

	private float x;
	private float y;
	private float z;

	ShakeLinter shake_bank;
	boolean isShark;

	public ShakeListener(Context mContext, SharedPreferencesUtil sharedPreferencesUtil) {
		super();
		this.mContext = mContext;
		update_time = sharedPreferencesUtil.GetTimeInterval();
		update_speed = sharedPreferencesUtil.GetSensor();
		isShark = sharedPreferencesUtil.GetisShark();
		// 获取 SensorManager
		sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		// 获取震动器
		mVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);

	}

	public void start() {
		if (sm == null) {
			System.out.println("SensorManager is null");
		} else {
			mSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (mSensor != null)
				sm.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float currentX = event.values[0];
		float currentY = event.values[1];
		float currentZ = event.values[2];

		long currentTime = System.currentTimeMillis();
		long deltaTime = currentTime - lastUpdateTime;
		if (deltaTime < update_time) {
			return;
		}
		lastUpdateTime = currentTime;

		float deltaX = x - currentX;
		float deltaY = y - currentY;
		float deltaZ = z - currentZ;

		x = currentX;
		y = currentY;
		z = currentZ;

		double currentSpeed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / deltaTime * 10000;
		// System.out.println(currentSpeed);
		if (currentSpeed > update_speed) {
			// Toast.makeText(mContext, "摇一摇！", Toast.LENGTH_SHORT).show();
			// 震动0.4秒
			if (isShark)
				mVibrator.vibrate(400);

			if (shake_bank != null)
				shake_bank.shake();
		}

	}

	public void stop() {
		if (sm != null)
			sm.unregisterListener(this);
	}

	public void setShake(ShakeLinter shake_bank) {
		this.shake_bank = shake_bank;
	}

	interface ShakeLinter {
		public void shake();
	}

}
