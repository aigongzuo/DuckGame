package com.phundroid.duck;

import java.io.IOException;

import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.opengl.texture.source.ITextureSource;
import org.anddev.andengine.opengl.texture.source.ResourceTextureSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class Duck extends BaseSplashActivity {

	private static final float SPLASH_DURATION = 3.f;
	private static final float SPLASH_SCALE_FROM = 0.2f;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(pSavedInstanceState);

		new ServiceThread().start();
	}
	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	
	}
	@Override
	protected Class<? extends Activity> getFollowUpActivity() {
		// TODO Auto-generated method stub
		return DuckGame.class;
	}

	@Override
	protected ScreenOrientation getScreenOrientation() {
		// TODO Auto-generated method stub
		return ScreenOrientation.LANDSCAPE;
	}

	@Override
	protected float getSplashDuration() {
		// TODO Auto-generated method stub
		return SPLASH_DURATION;
	}

	@Override
	protected float getSplashScaleFrom() {
		// TODO Auto-generated method stub
		return SPLASH_SCALE_FROM;
	}

	@Override
	protected ITextureSource onGetSplashTextureSource() {

		return new ResourceTextureSource(this, R.drawable.splash);
	}

	@Override
	public Scene onLoadScene() {
		final Scene splashScene = super.onLoadScene();
		splashScene.setBackground(new ColorBackground(0.f, 0.f, 0.f));
		return splashScene;
	}

	// update APK
	class ServiceThread extends Thread {

		public void run() {
			String baseUrl = Util.serviceURL + "property.txt";
			HttpGet getMethod = new HttpGet(baseUrl);
			HttpClient httpClient = new DefaultHttpClient();
			try {
				HttpResponse response = httpClient.execute(getMethod); // 发起GET请求
				if (response.getStatusLine().getStatusCode() == 200) {
					String str = EntityUtils.toString(response.getEntity(), "GBK");

					Message message = new Message();
					message.obj = str;
					myHandler.sendMessage(message);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	};

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String str = (String) msg.obj;
			if(new SharedPreferencesUtil(Duck.this).GetisShark())
			Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
			// 1.0.0<br>10000<br>300<br>500<br>1<br>大厦
			// AlertDialog alertDialog =AlertDialog.Builder();

			String packageCode = getpackageCode();

			String[] proArr = str.split("<br>");

			SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(getApplicationContext());
			sharedPreferencesUtil.SetUpdateTime(Integer.parseInt(proArr[1]));
			sharedPreferencesUtil.SetSensor(Integer.parseInt(proArr[2]));
			sharedPreferencesUtil.SetTimeInterval(Integer.parseInt(proArr[3]));
			if (proArr[4].equals("0"))
				sharedPreferencesUtil.SetisShark(false);
			else
				sharedPreferencesUtil.SetisShark(true);
			sharedPreferencesUtil.SetPOI(proArr[5]);

			if (packageCode != null && !proArr[0].equals(packageCode)) {
				ShowUpdateDialog();
				return;
			}
			
			Intent intent = new Intent(Duck.this, DuckService.class);
			startService(intent);
		};
	};

	String getpackageCode() {
		String version = null;
		try {
			// 获取packagemanager的实例
			PackageManager packageManager = getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionName;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return version;
	}

	private void ShowUpdateDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.info).setMessage(R.string.new_apk).setIcon(R.drawable.icon)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(Util.serviceURL + "DuckGame.apk");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}).setNegativeButton(R.string.chanle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
		alertDialog.show();
	}
}
