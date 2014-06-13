package com.phundroid.duck;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class Location {
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	LocalListener listener;

	public Location(Context context) {

		// 定位初始化
		mLocClient = new LocationClient(context);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(new SharedPreferencesUtil(context).getUpdateTime());
		mLocClient.setLocOption(option);
		mLocClient.start();

		// LocationClientOption option = new LocationClientOption();
		// option.setOpenGps(true);
		// option.setAddrType("all");//返回的定位结果包含地址信息
		// option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
		// option.disableCache(true);//禁止启用缓存定位
		// option.setPoiNumber(5); //最多返回POI个数
		// option.setPoiDistance(1000); //poi查询距离
		// option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
		// mLocClient.setLocOption(option);
	}

	/**
	 * 定位SDK监听函数
	 */
	public static double nlatitude;
	public static double nlontitude;

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null)
				return;
			nlatitude = location.getLatitude();
			nlontitude = location.getLongitude();
			if(listener!= null) {
				listener.onGPSUpdate(nlatitude, nlontitude);
			}
			
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			}

			Log.i("kang", sb.toString());
			// MyLocationData locData = new MyLocationData.Builder()
			// .accuracy(location.getRadius())
			// // 此处设置开发者获取到的方向信息，顺时针0-360
			// .direction(100).latitude(location.getLatitude())
			// .longitude(location.getLongitude()).build();
			// LatLng ll = new
			// LatLng(location.getLatitude(),location.getLongitude());
			// MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	public void destory() {
		// 退出时销毁定位
		mLocClient.stop();
	}

	public void setListener(LocalListener listener) {
		this.listener = listener;
	}

	interface LocalListener {
		public void onGPSUpdate(double nlatitude,double nlontitude);
	}
}
