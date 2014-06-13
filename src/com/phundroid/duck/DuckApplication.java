package com.phundroid.duck;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.umeng.analytics.MobclickAgent;

public class DuckApplication extends Application {
	Context mContext;
	static DuckApplication duckApplication;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		duckApplication = this;
		SDKInitializer.initialize(this);

//		Intent intent = new Intent(this, DuckService.class);
//		stopService(intent);
//		new ServiceThread().start();
	}

//	// update APK
//	class ServiceThread extends Thread {
//
//		public void run() {
//			String baseUrl = Util.serviceURL + "property.txt";
//			HttpGet getMethod = new HttpGet(baseUrl);
//			HttpClient httpClient = new DefaultHttpClient();
//			try {
//				HttpResponse response = httpClient.execute(getMethod); // 发起GET请求
//				if (response.getStatusLine().getStatusCode() == 200) {
//					String str = EntityUtils.toString(response.getEntity(), "GBK");
//
//					Message message = new Message();
//					message.obj = str;
//					myHandler.sendMessage(message);
//				}
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		};
//	};
//
//	Handler myHandler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			String str = (String) msg.obj;
//			Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
//			// 1.0.0<br>10000<br>300<br>500<br>1<br>大厦
//			// AlertDialog alertDialog =AlertDialog.Builder();
//
//			String packageCode = getpackageCode();
//
//			String[] proArr = str.split("<br>");
//
//			SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(getApplicationContext());
//			sharedPreferencesUtil.SetUpdateTime(Integer.parseInt(proArr[1]));
//			sharedPreferencesUtil.SetSensor(Integer.parseInt(proArr[2]));
//			sharedPreferencesUtil.SetTimeInterval(Integer.parseInt(proArr[3]));
//			if(proArr[4].equals("0"))
//				sharedPreferencesUtil.SetisShark(false);
//			else 
//				sharedPreferencesUtil.SetisShark(true);
//			sharedPreferencesUtil.SetPOI(proArr[5]);
//			
//			if (packageCode != null && !proArr[0].equals(packageCode)) {
//				ShowUpdateDialog();
//				return;
//			}
//
//			Intent intent = new Intent(DuckApplication.this, DuckService.class);
//			startService(intent);
//		};
//	};
//
//	String getpackageCode() {
//		String version = null;
//		try {
//			// 获取packagemanager的实例
//			PackageManager packageManager = getPackageManager();
//			// getPackageName()是你当前类的包名，0代表是获取版本信息
//			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
//			version = packInfo.versionName;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return version;
//	}
//
//	private void ShowUpdateDialog() {
//		AlertDialog alertDialog = new AlertDialog.Builder(mContext).setTitle(R.string.info).setMessage(R.string.new_apk).setIcon(R.drawable.icon)
//				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Uri uri = Uri.parse(Util.getURL(Util.serviceURL + "DuckGame.apk"));
//						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//						startActivity(intent);
//					}
//				}).setNegativeButton(R.string.chanle, new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//
//					}
//				}).create();
//		alertDialog.show();
//	}
}
