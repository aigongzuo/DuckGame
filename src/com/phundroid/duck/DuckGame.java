package com.phundroid.duck;

import java.io.IOException;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;
import com.umeng.analytics.MobclickAgent;

public class DuckGame extends LayoutGameActivity implements
		IAccelerometerListener {

	protected static final String PREFS_NAME = "GAME_SETTINGS";
	protected static final String USE_SOUND = "SOUND";
	protected static final String USE_VIBRO = "VIBRO";
	protected static final String USE_ACC = "ACC";
	protected static final String USE_TOUCH = "TOUCH";

	protected static final String GAME_SCORE = "GAME_SCORE";
	protected static final String USE_FIRST = "FIRST";
	protected static final String USE_SECOND = "SECOND";
	protected static final String USE_THIRD = "THIRD";

	public static int CAMERA_WIDTH = 800;// 480;
	public static int CAMERA_HEIGHT = 480;// 320;

	private Camera mCamera;
	private Texture mTexture, mTexture2, mTexture3;
	public static TextureRegion /* background, */
	hills, sky, largeCloud, mediumCloud, smallCloud, grass, aim, barrel,
			feather, featherFlipped, shootbutton, shell, flash, shot, logo,
			startButton, settingsButton, scoresButton;
	public static TiledTextureRegion duckFly, duckFlyFlipped;

	public static Sound sGroundHit, sDuckQuack, sShotReload;// ,
	// sAmbient;

	public static Music sAmbient;

	public static Font mDuckFont;
	public static Texture mDuckFontTexture;

	private Game game;
	// private ImageButton btnScoreloop;
	// private ImageButton btnSettings;
	private boolean musicPaused;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			ScoreloopManagerSingleton.init(this);
		} catch (IllegalStateException e) {
			Log.e("Init scoreloop", e.getMessage());
		}

		// btnScoreloop = (ImageButton)findViewById(R.id.scoreLoopBtn);
		// btnScoreloop.setOnClickListener(new OnClickListener()
		// {
		//
		// public void onClick(View arg0) {
		// if(findViewById(R.id.settingsDialog).getVisibility()==View.GONE){
		// game.setLoadComplete(false);
		// startActivity(new Intent(DuckGame.this, EntryScreenActivity.class));
		// }
		// }
		//
		// });

		// btnSettings = (ImageButton)findViewById(R.id.settings);
		// btnSettings.setOnClickListener(new OnClickListener()
		// {
		//
		// public void onClick(View arg0) {
		// findViewById(R.id.settingsDialog).setVisibility(View.VISIBLE);
		//
		// SharedPreferences settings =
		// getSharedPreferences(DuckGame.PREFS_NAME, 0);
		//
		// ((CheckBox)findViewById(R.id.settSoundEnable)).setChecked(settings.getBoolean(DuckGame.USE_SOUND,
		// true));
		// ((CheckBox)findViewById(R.id.settVibroEnable)).setChecked(settings.getBoolean(DuckGame.USE_VIBRO,
		// true));
		// ((RadioButton)findViewById(R.id.useAccelerometer)).setChecked(settings.getBoolean(DuckGame.USE_ACC,
		// true));
		// ((RadioButton)findViewById(R.id.useTouchScreen)).setChecked(settings.getBoolean(DuckGame.USE_TOUCH,
		// false));
		// }
		//
		// });

		final LinearLayout scoresLayout = (LinearLayout) findViewById(R.id.scoreDialog);
		final LinearLayout restartLayout = (LinearLayout) findViewById(R.id.restartDialog);

		TextView scoreTextCtrl = (TextView) scoresLayout
				.findViewById(R.id.scoreText);
		scoreTextCtrl.setText("保存得分？ (" + 0 + ")");

		Button global = (Button) scoresLayout.findViewById(R.id.globalButton);
		global.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				ScoreloopManagerSingleton.get().onGamePlayEnded(
						game.getScore(), null);
				// save scores global

				SharedPreferences scores = getSharedPreferences(GAME_SCORE, 0);
				SharedPreferences.Editor editor = scores.edit();

				int first = Integer.parseInt(scores.getString(USE_FIRST, "0"));
				int second = Integer.parseInt(scores.getString(USE_SECOND, "0"));
				double score = game.getScore();
				int temp = (int) score;
				if (game.getScore() > first) {
					editor.putString(USE_FIRST, temp + "");
					editor.putString(USE_SECOND, first + "");
					editor.putString(USE_THIRD, second + "");
				} else if (game.getScore() > second) {
					editor.putString(USE_SECOND, temp + "");
					editor.putString(USE_THIRD, second + "");
				} else {
					editor.putString(USE_THIRD, temp + "");
				}
				editor.commit();
				scoresLayout.setVisibility(View.GONE);
				restartLayout.setVisibility(View.VISIBLE);
			}
		});

		// Button local = (Button)scoresLayout.findViewById(R.id.localButton);
		// local.setOnClickListener(new OnClickListener() {
		// public void onClick(View arg0) {
		// scoresLayout.setVisibility(View.GONE);
		// restartLayout.setVisibility(View.VISIBLE);
		// }
		// });

		Button skip = (Button) scoresLayout.findViewById(R.id.skipButton);
		skip.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				scoresLayout.setVisibility(View.GONE);
				restartLayout.setVisibility(View.VISIBLE);
			}
		});

		Button restart = (Button) restartLayout.findViewById(R.id.replayBtn);
		restart.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				DuckGame.this.onGameResumed();
				if (game != null) {
					game.goToGameOrMenu(true);
					if (getSharedPreferences(DuckGame.PREFS_NAME, 0)
							.getBoolean(DuckGame.USE_VIBRO, true))
						getEngine().vibrate(1000);
				}
				restartLayout.setVisibility(View.GONE);
			}
		});

		Button menu = (Button) restartLayout.findViewById(R.id.menuBtn);
		menu.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				restartLayout.setVisibility(View.GONE);
				DuckGame.this.onGameResumed();
				game.goToGameOrMenu(false);
				// btnScoreloop.setVisibility(View.VISIBLE);
				// btnSettings.setVisibility(View.VISIBLE);
			}
		});

		Button save = (Button) findViewById(R.id.settSaveBtn);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				findViewById(R.id.settingsDialog).setVisibility(View.GONE);

				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();

				boolean useSound, useVibro, accelerometer, touchScreen;

				useSound = ((CheckBox) findViewById(R.id.settSoundEnable))
						.isChecked();
				useVibro = ((CheckBox) findViewById(R.id.settVibroEnable))
						.isChecked();
				accelerometer = ((RadioButton) findViewById(R.id.useAccelerometer))
						.isChecked();
				touchScreen = ((RadioButton) findViewById(R.id.useTouchScreen))
						.isChecked();

				if (accelerometer) {
					touchScreen = false;
				}
				if (touchScreen) {
					accelerometer = false;
				}

				editor.putBoolean(USE_SOUND, useSound);
				editor.putBoolean(USE_VIBRO, useVibro);
				editor.putBoolean(USE_ACC, accelerometer);
				editor.putBoolean(USE_TOUCH, touchScreen);

				if (useSound) {
					if (!DuckGame.sAmbient.isPlaying()) {
						DuckGame.sAmbient.play();
					}
				} else {
					if (DuckGame.sAmbient.isPlaying())
						DuckGame.sAmbient.pause();
				}
				// editor.putBoolean(IS_OPEN+level.getLevelId(), true);
				// editor.putBoolean(IS_OPEN+level.getNextLevel() , true);

				// Commit the edits!
				editor.commit();
			}
		});

		Button close = (Button) findViewById(R.id.settCloseBtn);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				findViewById(R.id.settingsDialog).setVisibility(View.GONE);
			}
		});

		Button exit = (Button) findViewById(R.id.backExitButton);
		exit.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});

		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				findViewById(R.id.historyScoresDialog).setVisibility(View.GONE);
			}
		});
		// Button menuResume = (Button)findViewById(R.id.backMenuResumeButton);
		// menuResume.setOnClickListener(new OnClickListener() {
		// public void onClick(View arg0) {
		// findViewById(R.id.backMenuDialog).setVisibility(View.GONE);
		// }
		// });
		Button backMenu = (Button) findViewById(R.id.backMenuButton);
		backMenu.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				game.goToGameOrMenu(false);
				findViewById(R.id.backMenuDialog).setVisibility(View.GONE);
			}
		});

		Button backResume = (Button) findViewById(R.id.backResumeButton);
		backResume.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				game.unPauseGame();
				findViewById(R.id.backMenuDialog).setVisibility(View.GONE);
			}
		});

	}

	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay();

		CAMERA_WIDTH = display.getWidth();
		CAMERA_HEIGHT = display.getHeight();

		if (CAMERA_WIDTH <= 480 && CAMERA_HEIGHT <= 320) {
			CAMERA_WIDTH = 480;
			CAMERA_HEIGHT = 320;
		}

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				this.mCamera).setNeedsSound(true).setNeedsMusic(true));
	}

	public void onLoadResources() {
		Log.i("WIDTHxHEIGHT", CAMERA_WIDTH + "x" + CAMERA_HEIGHT);
		if (CAMERA_WIDTH <= 480 && CAMERA_HEIGHT <= 320) {
			this.mTexture = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background480hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background480sky.png", 0, 306);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background480grass.png", 0,
					642);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel480.png", 802, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs480.png", 802, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes480.png", 0,
					642 + 82, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory
					.createFromAsset(this.mTexture, this,
							"graphics/shell480.png", 322, 642 + 82);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton480.png",
					322 + 50, 642 + 82);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash480.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot480.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title480.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/startbutton.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/settingsbutton.png", 0,
					226 + 50);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/scoresbutton.png", 0,
					226 + 50 + 50);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 28,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		} else if (CAMERA_WIDTH == 960 && CAMERA_HEIGHT == 540) {
			this.mTexture = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background960hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background960sky.png", 0, 600);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background960grass.png", 0,
					642);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel480.png", 960, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs480.png", 960, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes480.png", 0,
					642 + 120, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shell480.png", 322,
					642 + 120);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton480.png",
					322 + 50, 642 + 120);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash480.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot480.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title480.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/startbutton.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/settingsbutton.png", 0,
					226 + 50);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/scoresbutton.png", 0,
					226 + 50 + 50);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 48,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		} else if (CAMERA_WIDTH == 1280 && CAMERA_HEIGHT == 720) {
			this.mTexture = new Texture(2048, 2048, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1280hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background1280sky.png", 0, 1300);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1280grass.png", 0,
					642);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel800.png", 1280, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs800.png", 1280, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes800.png", 0,
					642 + 182, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shell800.png", 322,
					642 + 182);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton800.png",
					322 + 50, 642 + 182);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash800.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot800.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title800.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/start1280.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/setting1280.png", 0,
					226 + 150);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/score1280.png", 0,
					226 + 150 + 150);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 48,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		} else if (CAMERA_WIDTH == 1280 && CAMERA_HEIGHT == 768) {
			this.mTexture = new Texture(2048, 2048, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1280hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background1280sky.png", 0, 1300);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1280grass.png", 0,
					642);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel800.png", 1280, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs800.png", 1280, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes800.png", 0,
					642 + 182, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shell800.png", 322,
					642 + 182);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton800.png",
					322 + 50, 642 + 182);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash800.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot800.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title800.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/start1280.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/setting1280.png", 0,
					226 + 150);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/score1280.png", 0,
					226 + 150 + 150);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 48,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		} else if (CAMERA_WIDTH == 1920 && CAMERA_HEIGHT == 1080) {// 1920
			this.mTexture = new Texture(4096, 4096, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1920hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background1920sky.png", 0, 1600);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background1920grass.png", 0,
					800);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel800.png", 1920, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs800.png", 1920, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes800.png", 0,
					642 + 382, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shell800.png", 322,
					642 + 382);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton1920.png",
					322 + 50, 642 + 382);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash800.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot800.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title800.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/start1280.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/setting1280.png", 0,
					226 + 150);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/score1280.png", 0,
					226 + 150 + 150);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 48,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		} else// if(CAMERA_WIDTH==800 && CAMERA_HEIGHT==480) 都不是应用480 800
		{
			this.mTexture = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.hills = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background800hills.png", 0,
					0);
			DuckGame.sky = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/background800sky.png", 0, 306);
			DuckGame.grass = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/background800grass.png", 0,
					642);
			DuckGame.barrel = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/barrel800.png", 802, 0);
			DuckGame.aim = TextureRegionFactory.createFromAsset(this.mTexture,
					this, "graphics/crosshairs800.png", 802, 352);
			DuckGame.duckFly = TextureRegionFactory.createTiledFromAsset(
					this.mTexture, this, "graphics/duckallframes800.png", 0,
					642 + 82, 4, 4);
			DuckGame.duckFlyFlipped = duckFly.clone();
			DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
			DuckGame.shell = TextureRegionFactory
					.createFromAsset(this.mTexture, this,
							"graphics/shell800.png", 322, 642 + 82);
			DuckGame.shootbutton = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/shootbutton800.png",
					322 + 50, 642 + 82);
			DuckGame.feather = TextureRegionFactory.createFromAsset(
					this.mTexture, this, "graphics/feather.png", 0,
					642 + 82 + 256);
			DuckGame.featherFlipped = feather.clone();
			DuckGame.featherFlipped.setFlippedHorizontal(true);
			this.mEngine.getTextureManager().loadTexture(this.mTexture);

			// no enough space in mTexture left :)
			this.mTexture2 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.largeCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/largecloud.png", 0, 0);
			DuckGame.mediumCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/mediumcloud.png", 242, 0);
			DuckGame.smallCloud = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/smallcloud.png", 242 + 130,
					0);
			DuckGame.flash = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/muzzleflash800.png", 0, 66);
			DuckGame.shot = TextureRegionFactory.createFromAsset(
					this.mTexture2, this, "graphics/shot800.png", 98, 66);
			this.mEngine.getTextureManager().loadTexture(this.mTexture2);

			this.mTexture3 = new Texture(1024, 1024, TextureOptions.BILINEAR);
			DuckGame.logo = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/title800.png", 0, 0);
			DuckGame.startButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/startbutton.png", 0, 226);
			DuckGame.settingsButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/settingsbutton.png", 0,
					226 + 50);
			DuckGame.scoresButton = TextureRegionFactory.createFromAsset(
					this.mTexture3, this, "graphics/scoresbutton.png", 0,
					226 + 50 + 50);
			this.mEngine.getTextureManager().loadTexture(this.mTexture3);

			DuckGame.mDuckFontTexture = new Texture(256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			DuckGame.mDuckFont = FontFactory.createFromAsset(
					DuckGame.mDuckFontTexture, this, "comixheavy.ttf", 48,
					true, Color.BLACK);
			this.mEngine.getTextureManager().loadTexture(
					DuckGame.mDuckFontTexture);
			this.mEngine.getFontManager().loadFont(DuckGame.mDuckFont);
		}

		SoundFactory.setAssetBasePath("sounds/");
		MusicFactory.setAssetBasePath("sounds/");
		try {
			DuckGame.sGroundHit = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "duckgroundhit.ogg");
			DuckGame.sDuckQuack = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "duckquack.ogg");
			DuckGame.sShotReload = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "gunshotreload.ogg");
			DuckGame.sAmbient = MusicFactory.createMusicFromAsset(
					this.mEngine.getMusicManager(), this,
					"background_country_ambience_loop.ogg");
			// 11-25 17:20:25.280: ERROR/AudioCache(31): Heap size overflow! req
			// size: 1052672, max size: 1048576

		} catch (final IOException e) {
			Debug.e("Error", e);
		}

		// AdManager.setTestDevices(new String[]
		// {
		// AdManager.TEST_EMULATOR
		// });
	}

	public void onLoadComplete() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (game != null) {
			return game.onKeyDown(keyCode);
		}

		return true;
	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	//
	// if(game!=null){game.onKeyUp(keyCode);}
	//
	// return super.onKeyUp(keyCode, event);
	// }

	public Scene onLoadScene() {
		// this.mEngine.registerUpdateHandler(new FPSLogger());
		final Scene scene = new Scene(3);

		game = new Game(scene, DuckGame.this);

		scene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float secondsElapsed) {
				game.update();
			}

			public void reset() {

			}

		});

		this.mEngine.enableAccelerometerSensor(this, this);// enable
															// accelerometer
		this.mEngine.enableVibrator(this);

		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {

			public boolean onSceneTouchEvent(Scene scene,
					TouchEvent sceneTouchEvent) {

				game.onTouch(sceneTouchEvent);

				return true;
			}
		});

		return scene;
	}

	public void onAccelerometerChanged(AccelerometerData accelerometerData) {
		if (game != null) {
			game.onAccelerometerChanged(accelerometerData);
		}
	}

	@Override
	protected int getLayoutID() {
		return R.layout.main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.andengine_surface;
	}

	public void hideScoreLoop(final boolean hide, final boolean hideLoading) {
		runOnUiThread(new Thread() {
			@Override
			public void run() {
				// if(hide)
				// {
				// btnScoreloop.setVisibility(View.GONE);
				// btnSettings.setVisibility(View.GONE);
				// }
				// else
				// {
				// btnScoreloop.setVisibility(View.VISIBLE);
				// btnSettings.setVisibility(View.VISIBLE);
				// }

				if (hideLoading) {
					ImageView loadImg = (ImageView) findViewById(R.id.loadingImage);
					loadImg.setVisibility(View.GONE);
				}
			}
		});

	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
		if (sAmbient != null) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

			if (settings.getBoolean(USE_SOUND, true)) {
				try {
					sAmbient.pause();
					musicPaused = true;
				} catch (Exception e) {
				}
			}
		}
		if (game != null) {
			if (game.getGAME_STATE() == Game.STATE_RUN) {
				game.pauseGame();
			}
		}
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
		
		// btnScoreloop.setVisibility(View.GONE);
		// btnSettings.setVisibility(View.GONE);
		if (sAmbient != null && musicPaused) {
			sAmbient.resume();
			musicPaused = false;
		}
		if (game != null) {
			game.setLoadComplete(false);
			if (game.isPaused()) {
				game.unPauseGame();
			}
		}
		findViewById(R.id.loadingImage).setVisibility(View.VISIBLE);
	}
}