package com.phundroid.duck;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.phundroid.duck.Location.LocalListener;
import com.phundroid.duck.ShakeListener.ShakeLinter;

public class DuckService extends Service implements ShakeLinter, OnGetPoiSearchResultListener, LocalListener {

	ShakeListener listener = null;

	private PoiSearch mPoiSearch = null;
	Location location;
	SharedPreferencesUtil sharedPreferencesUtil;

	String Device_id;

	@Override
	public void onCreate() {
		super.onCreate();
		sharedPreferencesUtil = new SharedPreferencesUtil(this);
		sharedPreferencesUtil.SetStep(0);

		Device_id = Util.getSingleID(this);
		
		listener = new ShakeListener(this, sharedPreferencesUtil);
		listener.start();
		listener.setShake(this);

		location = new Location(getApplicationContext());
		location.setListener(this);

		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	public void startSearch() {
		PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();
		poiNearbySearchOption.keyword(sharedPreferencesUtil.GetPOI());
		poiNearbySearchOption.location(new LatLng(Location.nlatitude, Location.nlontitude));
		poiNearbySearchOption.pageCapacity(10);
		poiNearbySearchOption.pageNum(0);
		poiNearbySearchOption.radius(1000);
		mPoiSearch.searchNearby(poiNearbySearchOption);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("kang", "onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i("kang", "onDestroy");
		if (listener != null)
			listener.stop();
		if (location != null)
			location.destory();
		if (mPoiSearch != null)
			mPoiSearch.destroy();
	}

	@Override
	public void shake() {
		sharedPreferencesUtil.SetStep(sharedPreferencesUtil.GetStep() + 1);
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			List<PoiInfo> retList = result.getAllPoi();
			String str = "";
			double length = 0;
			double lastDouble = 10000;
			for (PoiInfo poiInfo : retList) {
				length = DistanceUtil.getDistance(new LatLng(Location.nlatitude, Location.nlontitude), new LatLng(poiInfo.location.latitude, poiInfo.location.longitude));
				if (lastDouble > length) {
					str = poiInfo.name;
					lastDouble = length;
					str += ("(" + (int)length+"米)");
				}
			}

			new ServiceThread(str).start();
			if(sharedPreferencesUtil.GetisShark())
			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
			return;
		}
		// if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
		//
		// // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
		// String strInfo = "在";
		// for (CityInfo cityInfo : result.getSuggestCityList()) {
		// strInfo += cityInfo.city;
		// strInfo += ",";
		// }
		// strInfo += "找到结果";
		// Toast.makeText(PoiSearchDemo.this, strInfo,
		// Toast.LENGTH_LONG).show();
		// }
	}

	@Override
	public void onGPSUpdate(double nlatitude, double nlontitude) {
		startSearch();
	}

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String str = (String) msg.obj;
				showNotification(str);
				break;

			default:
				break;
			}

		};
	};

	public void showNotification(String str) {
		Uri uri = Uri.parse(Util.getURL(str)+"?phonenumber="+Util.getPhoneNumber(this)+"&identification="+Util.getSingleID(this));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;

		notification.setLatestEventInfo(getApplicationContext(), Util.getTitle(str), Util.getContext(str), pendingIntent);
		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mgr.notify(Util.getNOID(str), notification);
	}

	class ServiceThread extends Thread {
		String keyword;

		ServiceThread(String keyword) {
			this.keyword = keyword;
		}

		public void run() {
			String baseUrl = null;
			try {
				String str = Util.serviceURL + "message?step=" + sharedPreferencesUtil.GetStep() + "&keyword=" + URLEncoder.encode(keyword, "UTF-8") + "&deviceID=" + Device_id+"&code=" + sharedPreferencesUtil.GetNOID();
				Log.i("kang", str);
				baseUrl = (str);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			HttpGet getMethod = new HttpGet(baseUrl);
			HttpClient httpClient = new DefaultHttpClient();
			try {
				HttpResponse response = httpClient.execute(getMethod); // 发起GET请求
				if (response.getStatusLine().getStatusCode() == 200) {
					String str = URLDecoder.decode(EntityUtils.toString(response.getEntity(), "UTF-8"), "UTF-8"); // EntityUtils.toString(response.getEntity(),
																													// "utf-8");
					if (str.indexOf("<RN>0</RN>") == -1) {
						Message message = new Message();
						message.what = 0;
						message.obj = str;
						sharedPreferencesUtil.SetNOID(Util.getSTRNOID(str));
						myHandler.sendMessage(message);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	};
}
