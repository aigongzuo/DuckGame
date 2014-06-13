package com.phundroid.duck;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.collision.BaseCollisionChecker;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.util.GLHelper;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.badlogic.gdx.math.Vector2;

public class Game {

	private int GAME_STATE; 
	
	public static final int STATE_MENU=0;
	public static final int STATE_RUN=1;
	public static final int STATE_DIALOGS=2;
	
	private Random random = new Random();
	
	private ArrayList<Bird> ducks = new ArrayList<Bird>();
	private ArrayList<Bird> ducksToRemove = new ArrayList<Bird>();
	private ArrayList<Feather> feathers = new ArrayList<Feather>();
	private ArrayList<Feather> feathersBuffer = new ArrayList<Feather>();
	private ArrayList<Feather> feathersToRemove = new ArrayList<Feather>();
	
	private ArrayList<Cloud> clouds = new ArrayList<Cloud>();
	private int maxClouds = 5;
	
	private Sprite grass, /*background,*/shootbutton,hills,sky;
	private Sprite aim;
	private float aimXSpeed = 0;

	private float aimYSpeed = 0;
	private Sprite barrel;
	private Sprite flash;
	private Sprite shot;
	
	private int shells=3;
	private boolean updateShells = false;
	private ArrayList<Sprite> shellSprites = new ArrayList<Sprite>();
	
	private boolean roundOver = false;
	
	private final int ducksLeft = 20;//20
	private int ducksKilled = 0;
	private int ducksCreated = 0;
	private final int ducksToNextLevel = 16;//16

	private Double score = 0.0;
	private float birdSpeed = 1.0f;
	private Text scoreLabel;
	private Text nextLevelMsg;
	private ChangeableText scoreText;
	private ChangeableText ducksText;
	private ChangeableText currLvlText;
	private int currentLevel = 1;
	
	private Scene gameScene;
	//admob section
//	private boolean showAds = false;
//	private long adTimeLeft;
	private final DuckGame gameActivity;
	
	//menu section
	private Sprite logo,startButton, settingsButton, scoresButton;
	private boolean loadComplete = false;
	boolean useSound, useVibro, accelerometer, touchScreen;

	private boolean paused = false;

	private long startPauseTime;
	
	
	public Game(final Scene gameScene, final DuckGame ctx) {
		this.gameScene = gameScene;		
		gameActivity = ctx;
		
		GAME_STATE = STATE_MENU;
		
		sky = new Sprite(0,0,DuckGame.sky.getWidth(),DuckGame.sky.getHeight(),DuckGame.sky){
		      protected void onInitDraw(final GL10 pGL)
		      {
		         super.onInitDraw(pGL);
		         GLHelper.enableTextures(pGL);
		         GLHelper.enableTexCoordArray(pGL);
		         GLHelper.enableDither(pGL);
		      }
		};
		gameScene.getLayer(0).addEntity(sky);

		for(int i=0;i<maxClouds;i++)
		{
			Cloud cloud;
			float cloudSpeed = 1;

			switch(1+random.nextInt(3))
			{
			default:
				cloud = new Cloud(random.nextInt(700),random.nextInt(160),DuckGame.largeCloud.getWidth(),DuckGame.largeCloud.getHeight(),DuckGame.largeCloud);
				cloudSpeed = (10+random.nextInt(5))/10f;   //1.0 - 1.5
				break;
			case 2:
				cloud = new Cloud(random.nextInt(700),random.nextInt(120),DuckGame.mediumCloud.getWidth(),DuckGame.mediumCloud.getHeight(),DuckGame.mediumCloud);
				cloudSpeed = (5+random.nextInt(5))/10f;	//0.5 - 1.0					
				break;
			case 3:
				cloud = new Cloud(random.nextInt(700),random.nextInt(80),DuckGame.smallCloud.getWidth(),DuckGame.smallCloud.getHeight(),DuckGame.smallCloud);
				cloudSpeed = (1+random.nextInt(4))/10f;	//0.1 - 0.5
				break;
			}

			cloud.setCloudSpeed(cloudSpeed);
			clouds.add(cloud);			
			gameScene.getLayer(0).addEntity(cloud);
				
		}
		
				
		hills = new Sprite(0,DuckGame.CAMERA_HEIGHT-DuckGame.hills.getHeight(),DuckGame.hills.getWidth(),DuckGame.hills.getHeight(),DuckGame.hills){
		      protected void onInitDraw(final GL10 pGL)
		      {
		         super.onInitDraw(pGL);
		         GLHelper.enableTextures(pGL);
		         GLHelper.enableTexCoordArray(pGL);
		         GLHelper.enableDither(pGL);
		      }
		};
		gameScene.getLayer(1).addEntity(hills);
		
		//create grass
		grass = new Sprite(0,DuckGame.CAMERA_HEIGHT-DuckGame.grass.getHeight(),DuckGame.grass.getWidth(),DuckGame.grass.getHeight(),DuckGame.grass);
		gameScene.getLayer(2).addEntity(grass);
		

		//create barrel
		flash = new Sprite(DuckGame.CAMERA_WIDTH/2-DuckGame.flash.getWidth()/2,DuckGame.CAMERA_HEIGHT-DuckGame.flash.getHeight()/2,DuckGame.flash.getWidth(),DuckGame.flash.getHeight(),DuckGame.flash);
		flash.setAlpha(0.0f);
		gameScene.getLayer(2).addEntity(flash);
		
		shot = new Sprite(DuckGame.CAMERA_WIDTH/2-DuckGame.shot.getWidth()/2,DuckGame.CAMERA_HEIGHT-DuckGame.shot.getHeight()/2,DuckGame.shot.getWidth(),DuckGame.shot.getHeight(),DuckGame.shot);
		shot.setAlpha(0.0f);
		gameScene.getLayer(2).addEntity(shot);
		
		//create crosshair
		aim = new Sprite(DuckGame.CAMERA_WIDTH/2-DuckGame.aim.getWidth()/2,DuckGame.CAMERA_HEIGHT/2-DuckGame.aim.getHeight()/2,DuckGame.aim.getWidth(),DuckGame.aim.getHeight(),DuckGame.aim);		
		gameScene.getLayer(2).addEntity(aim);
		
		barrel = new Sprite(DuckGame.CAMERA_WIDTH/2-DuckGame.barrel.getWidth()/2,DuckGame.CAMERA_HEIGHT-DuckGame.barrel.getHeight()/2,DuckGame.barrel.getWidth(),DuckGame.barrel.getHeight(),DuckGame.barrel);		
		gameScene.getLayer(2).addEntity(barrel);
		
		//shootbutton
		shootbutton = new Sprite(DuckGame.CAMERA_WIDTH-DuckGame.shootbutton.getWidth()*1.5f,DuckGame.CAMERA_HEIGHT-DuckGame.shootbutton.getHeight()*1.4f,DuckGame.shootbutton.getWidth(),DuckGame.shootbutton.getHeight(),DuckGame.shootbutton);
		gameScene.getLayer(2).addEntity(shootbutton);
		gameScene.registerTouchArea(shootbutton);//touch
		
		gameScene.getLayer(1).addEntity(scoreLabel = new Text(25, 5, DuckGame.mDuckFont, "得分："));
		gameScene.getLayer(1).addEntity(scoreText = new ChangeableText(scoreLabel.getX()+scoreLabel.getWidth() + 10, 5, DuckGame.mDuckFont, "000000000000"));
		scoreText.setText("0");

		gameScene.getLayer(1).addEntity(nextLevelMsg = new Text(0, 0, DuckGame.mDuckFont, "Next level..."));
		nextLevelMsg.setPosition(DuckGame.CAMERA_WIDTH/2-nextLevelMsg.getWidth()/2,DuckGame.CAMERA_HEIGHT/2-nextLevelMsg.getHeight()/2);
		nextLevelMsg.setAlpha(0.0f);
		
		gameScene.getLayer(1).addEntity(ducksText = new ChangeableText(DuckGame.CAMERA_WIDTH, 5, DuckGame.mDuckFont, "0000000"));
		ducksText.setText("0/20");
		ducksText.setPosition(DuckGame.CAMERA_WIDTH-ducksText.getWidth()*1.2f, 5);
		
		gameScene.getLayer(1).addEntity(currLvlText = new ChangeableText(DuckGame.CAMERA_WIDTH, 5, DuckGame.mDuckFont, "0000000"));
		currLvlText.setText("Lvl."+currentLevel);
		currLvlText.setPosition(DuckGame.CAMERA_WIDTH-currLvlText.getWidth()*1.2f, ducksText.getY()+ducksText.getHeight()+5);
		
		updateShellsIndicator();
		
		DuckGame.sAmbient.setLooping(true);
		
		if(gameActivity.getSharedPreferences(DuckGame.PREFS_NAME, 0).getBoolean(DuckGame.USE_SOUND, true))
		{
			DuckGame.sAmbient.play();	
		}
		
		
//		showHideAdmobAds(false);
		
		//
		logo = new Sprite(DuckGame.CAMERA_WIDTH/2-DuckGame.logo.getWidth()/2,
						DuckGame.CAMERA_HEIGHT/2-DuckGame.logo.getHeight()/2
						-DuckGame.startButton.getHeight()/2,DuckGame.logo);
		gameScene.getTopLayer().addEntity(logo);

		startButton = new Sprite(DuckGame.CAMERA_WIDTH/2-(DuckGame.startButton.getWidth()+DuckGame.settingsButton.getWidth()+DuckGame.scoresButton.getWidth())/2,logo.getY()+logo.getHeight()+20,DuckGame.startButton);
		gameScene.getTopLayer().addEntity(startButton);
		gameScene.registerTouchArea(startButton);//touch

		scoresButton = new Sprite(startButton.getX()+startButton.getWidth()+5,logo.getY()+logo.getHeight()+20,DuckGame.scoresButton);
		gameScene.getTopLayer().addEntity(scoresButton);
		gameScene.registerTouchArea(scoresButton);//touch
		
		settingsButton= new Sprite(scoresButton.getX()+scoresButton.getWidth()+5,logo.getY()+logo.getHeight()+20,DuckGame.settingsButton);
		gameScene.getTopLayer().addEntity(settingsButton);
		gameScene.registerTouchArea(settingsButton);//touch
		

		//
		
		gameScene.setOnAreaTouchListener(new IOnAreaTouchListener()
		{
			public boolean onAreaTouched(final TouchEvent sceneTouchEvent,ITouchArea touchArea, float touchAreaLocalX,float touchAreaLocalY) 
			{
				if(!loadComplete)
				{
					return true;
				}
				
				if(sceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && startButton.isVisible()&&touchArea.equals(startButton))
				{
					if(gameActivity.findViewById(R.id.settingsDialog).getVisibility()==View.GONE
						&& gameActivity.findViewById(R.id.backMenuDialog).getVisibility()==View.GONE
						&& gameActivity.findViewById(R.id.historyScoresDialog).getVisibility()==View.GONE)
					{
						GAME_STATE = STATE_RUN;
						
						gameActivity.hideScoreLoop(true, true);
						
						logo.setVisible(false);
						startButton.setVisible(false);
						settingsButton.setVisible(false);
						scoresButton.setVisible(false);
						nextLevel(true);
						
						paused = false;
						
						SharedPreferences settings = gameActivity.getSharedPreferences(DuckGame.PREFS_NAME, 0);
						
						useSound = settings.getBoolean(DuckGame.USE_SOUND, true);
						useVibro = settings.getBoolean(DuckGame.USE_VIBRO, true);
						accelerometer = settings.getBoolean(DuckGame.USE_ACC, true);
						touchScreen = settings.getBoolean(DuckGame.USE_TOUCH, false);
						
						shootbutton.setVisible(accelerometer);
						
					}
				}
				else if(sceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && shells>0 && shootbutton.isVisible() && touchArea.equals(shootbutton) && !paused){
					//shot extras
					makeShoot();
				}
				else if(sceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && settingsButton.isVisible()&&touchArea.equals(settingsButton))
				{
					gameActivity.runOnUiThread(new Thread()
					{
						public void run() 
						{
							if(gameActivity.findViewById(R.id.backMenuDialog).getVisibility()==View.GONE
									&& gameActivity.findViewById(R.id.historyScoresDialog).getVisibility()==View.GONE){
								gameActivity.findViewById(R.id.settingsDialog).setVisibility(View.VISIBLE);
								
								SharedPreferences settings = gameActivity.getSharedPreferences(DuckGame.PREFS_NAME, 0);
								
								((CheckBox)gameActivity.findViewById(R.id.settSoundEnable)).setChecked(settings.getBoolean(DuckGame.USE_SOUND, true));
								((CheckBox)gameActivity.findViewById(R.id.settVibroEnable)).setChecked(settings.getBoolean(DuckGame.USE_VIBRO, true));
								((RadioButton)gameActivity.findViewById(R.id.useAccelerometer)).setChecked(settings.getBoolean(DuckGame.USE_ACC, true));
								((RadioButton)gameActivity.findViewById(R.id.useTouchScreen)).setChecked(settings.getBoolean(DuckGame.USE_TOUCH, false));
							}
						};
							
					});
					
				}
				else if(sceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN && scoresButton.isVisible()&&touchArea.equals(scoresButton))
				{
					gameActivity.runOnUiThread(new Thread()
					{
						public void run() 
						{
							if(gameActivity.findViewById(R.id.settingsDialog).getVisibility()==View.GONE
								&& gameActivity.findViewById(R.id.backMenuDialog).getVisibility()==View.GONE){
								
								gameActivity.findViewById(R.id.historyScoresDialog).setVisibility(View.VISIBLE);
								SharedPreferences scores = gameActivity.getSharedPreferences(DuckGame.GAME_SCORE, 0);
								
								((TextView)gameActivity.findViewById(R.id.tv_first)).setText(scores.getString(DuckGame.USE_FIRST, "0"));
								((TextView)gameActivity.findViewById(R.id.tv_second)).setText(scores.getString(DuckGame.USE_SECOND, "0"));
								((TextView)gameActivity.findViewById(R.id.tv_third)).setText(scores.getString(DuckGame.USE_THIRD, "0"));
//								((TextView)gameActivity.findViewById(R.id.useTouchScreen)).setChecked(scores.getBoolean(DuckGame.USE_TOUCH, false));
//								setLoadComplete(false);
//								gameActivity.startActivity(new Intent(gameActivity, EntryScreenActivity.class));
							}
						};
							
					});
				}
				
				return true;
			}

		});
		
		
		barrel.setVisible(false);
		aim.setVisible(false);
		flash.setVisible(false);
		shot.setVisible(false);
		currLvlText.setVisible(false);
		ducksText.setVisible(false);
		scoreLabel.setVisible(false);
		scoreText.setVisible(false);
		shootbutton.setVisible(false);
		for(Sprite shell:shellSprites)
		{
			shell.setVisible(false);
		}
	}
	
	private void makeShoot() {
		if(useSound)DuckGame.sShotReload.play();
		if(useVibro)gameActivity.getEngine().vibrate(100);
		flash.setAlpha(1.0f);
		
		shot.setPosition(aim.getX()+aim.getWidth()/2-shot.getWidth()/2, aim.getY()+aim.getHeight()/2-shot.getHeight()/2);
		shot.setAlpha(1.0f);
		//
		
		for(final Bird bird: ducks)
		{
			if(bird.isFlying() && aim.collidesWith(bird))
			{
//				>If bullet collides with duck the duck must die. 
//				if((bird.getX()+bird.getWidth()*0.2f>shot.getX()+shot.getWidth() && bird.getX()+bird.getWidth()*0.8f < shot.getX()+shot.getWidth()) &&
//				   (bird.getY()+bird.getHeight()*0.2f>shot.getY()+shot.getHeight()&& bird.getY()+bird.getHeight()*0.8f < shot.getY()+shot.getHeight()))
//				if(bird.collidesWith(shot))
				float birdCenterX =bird.getX()+bird.getWidth()/2;
				float birdCenterY =bird.getY()+bird.getHeight()/2;
				
				float shotCenterX =shot.getX()+shot.getWidth()/2;
				float shotCenterY =shot.getY()+shot.getHeight()/2;
				
				if(BaseCollisionChecker.checkAxisAlignedRectangleCollision(
						birdCenterX-bird.getWidth()*0.6f/2, 
						birdCenterY-bird.getHeight()*0.6f/2, 
						birdCenterX+bird.getWidth()*0.6f/2, 
						birdCenterY+bird.getHeight()*0.6f/2, 
						
						shotCenterX-shot.getWidth()*0.9f/2, 
						shotCenterY-shot.getHeight()*0.9f/2, 
						shotCenterX+shot.getWidth()*0.9f/2, 
						shotCenterY+shot.getHeight()*0.9f/2))
				{
					bird.setFalling(true);
					bird.stopAnimation(12);
					
//					score+=Math.abs((int)(100*bird.getXSpeed()));
					score+=((1-(bird.timeLeft()/bird.getLifeTime()))*100f);
					ducksKilled++;
					
					synchronized (feathersBuffer) {
						
						for(int i=0;i<10;i++)
						{
							float featherSize = 1+(1+random.nextInt(9))/10;
							//first duck
							Feather feather;
							if(random.nextBoolean())
							{
								feather = new Feather(/*-20*/bird.getX()-bird.getWidth()*featherSize/2+random.nextInt((int) (bird.getWidth())),
															 bird.getY()-bird.getHeight()*featherSize/2+random.nextInt((int) (bird.getHeight()))/*random.nextInt(320-40grass*/, 
										DuckGame.feather.getWidth()*featherSize, 
										DuckGame.feather.getHeight()*featherSize,DuckGame.feather);	
							}
							else
							{
								feather = new Feather(/*-20*/bird.getX()-bird.getWidth()*featherSize/2+random.nextInt((int) (bird.getWidth())),
										 bird.getY()-bird.getHeight()*featherSize/2+random.nextInt((int) (bird.getHeight()))/*random.nextInt(320-40grass*/, 
										 DuckGame.featherFlipped.getWidth()*featherSize, 
										 DuckGame.featherFlipped.getHeight()*featherSize,DuckGame.featherFlipped);
							}
							feathersBuffer.add(feather);
							gameScene.getLayer(1).addEntity(feather);
						}	
					}
					
					break;
				}
				else
				{
					bird.setWasScared(true);
					bird.animate(new long[]{100,100,100,100}, 8, 11, 4,new IAnimationListener()
					{

						public void onAnimationEnd(
								AnimatedSprite animatedSprite) {
							bird.animate(new long[]{100,100,100,100}, 0, 3, true);
						}
						
					});
				}
			}						
		}
		//
		shells--;
		if(shells<0)
		{
			shells=0;
		}
		updateShells = true;
		
		if(ducks.size()>0 && shells == 0)
		{
			roundOver = true;
		}
		//
	}
	
	public void update() {
		
		if(!loadComplete)
		{
			loadComplete = true;
			gameActivity.hideScoreLoop(false,true);

		}
		
		{//update clouds, independed to game started or menu
			for (Cloud cloud : clouds) {
				cloud.update();
			}
		}
		
//		if(showAds)
//		{
//			if(System.currentTimeMillis()>adTimeLeft)
//			{
//				showAds = false;
////				showHideAdmobAds(false);
//			}
//		}
		
		if(GAME_STATE == STATE_RUN && !paused)
		{
		
			updateCrosshair();
			updateFeathers();
						
			if(updateShells)
			{
				updateShellsIndicator();
				updateShells = false;
			}
			
			
			
			if(ducks.size() == 0 )
			{
				if(ducksCreated<ducksLeft)
				{
					//first duck
					Bird duck = new Bird(0, random.nextInt((int) (grass.getY()-grass.getHeight())), 
							DuckGame.duckFly.getTileWidth(), 
							DuckGame.duckFly.getTileHeight(),DuckGame.duckFly,useSound);
					
					ducks.add(duck);
					gameScene.getLayer(1).addEntity( duck);
					duck.setXSpeed(birdSpeed);
					DuckGame.duckFly.setFlippedHorizontal(false);//was a bug with flying bird backward :)
					duck.animate(new long[]{100,100,100,100}, 0, 3, true);
		
					//second duck flying from another side
					duck = new Bird(DuckGame.CAMERA_WIDTH-DuckGame.duckFlyFlipped.getTileWidth(),  random.nextInt((int) (grass.getY()-grass.getHeight())), 
							DuckGame.duckFlyFlipped.getTileWidth(), 
							DuckGame.duckFlyFlipped.getTileHeight(),DuckGame.duckFlyFlipped,useSound);				
					ducks.add(duck);
					duck.setXSpeed(-birdSpeed);
					gameScene.getLayer(1).addEntity(duck);
					DuckGame.duckFlyFlipped.setFlippedHorizontal(true);
					duck.animate(new long[]{100,100,100,100}, 0, 3, true);
					
					shells = 3;
					updateShells = true;
	
					ducksCreated+=2;
	
					//>After each level move the field so it gives the illusion of new a new scene. 
					//TODO: test it
	//				hills.getTextureRegion().setTexturePosition(hills.getTextureRegion().getTexturePositionX()+400,hills.getTextureRegion().getTexturePositionY()) ;
	//				DuckGame.hills.setTexturePosition(pX, pY);
				}
				else //start next level, commit scores, run once
				{
					if(ducksKilled>ducksToNextLevel)
					{
						nextLevel(false);
					}
					else //if run once
					{
						GAME_STATE = STATE_DIALOGS;
						
						for(Feather f: feathers)
						{
							gameScene.getLayer(1).removeEntity(f);
						}
						feathers.clear();
						
						gameActivity.runOnUiThread(new Thread()
						{
							@Override
							public void run() {
//								gameActivity.onShowDialog(DuckGame.DIALOG_LOCAL_GLOBAL_SCORES);
								LinearLayout scoresLayout = (LinearLayout)gameActivity.findViewById(R.id.scoreDialog);
								scoresLayout.setVisibility(View.VISIBLE);
								TextView scoreTextCtrl = (TextView)scoresLayout.findViewById(R.id.scoreText);
								scoreTextCtrl.setText("保存得分？ ("+score.intValue()+")");
							}
						});
								
					}
				}
			}
			else
			{
				for (Bird duck : ducks) {
					if(roundOver && !duck.isFalling() && !duck.isFallen())
					{
						duck.setFlyAway(true);
						
					}
					
					duck.update();
					
					if(duck.isFallen())
					{
						if(useSound)DuckGame.sGroundHit.play();
						ducksToRemove.add(duck);
					}
				}
				
				roundOver = false;
				
				for(Bird duck: ducksToRemove)
				{
					ducks.remove(duck);
					gameScene.getLayer(1).removeEntity(duck);
				}
				ducksToRemove.clear();
			}
		}
		else if(GAME_STATE == STATE_MENU)
		{
			if(ducks.size()>0)
			{
				for(Bird bird:ducks)
				{
					gameScene.getLayer(1).removeEntity(bird);				
				}
				ducks.clear();
			}
			
			if(feathers.size()>0)
			{
				for(Feather feather:feathers)
				{
					gameScene.getLayer(1).removeEntity(feather);				
				}
				feathers.clear();
			}
			
			if(feathersBuffer.size()>0)
			{
				for(Feather feather:feathersBuffer)
				{
					gameScene.getLayer(1).removeEntity(feather);				
				}
				feathersBuffer.clear();
			}
		}
	}

//	public void	onKeyUp(int keyCode) {
//		if(keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
//		{
//			aimYSpeed = 0;
//		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
//		{
//			aimXSpeed = 0;
//		}
//	}

	public boolean onKeyDown(int keyCode) {
		if(keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME)
		{
			gameActivity.runOnUiThread(new Thread()
			{
				@Override
				public void run() {
					if(GAME_STATE==STATE_RUN)
					{
						pauseGame();
						gameActivity.findViewById(R.id.backMenuDialog).setVisibility(View.VISIBLE);
//						gameActivity.findViewById(R.id.backMenuResumeButton).setVisibility(View.GONE);
						gameActivity.findViewById(R.id.backResumeButton).setVisibility(View.VISIBLE);
						gameActivity.findViewById(R.id.backMenuButton).setVisibility(View.VISIBLE);
					}
					else
					{
						gameActivity.findViewById(R.id.backMenuDialog).setVisibility(View.VISIBLE);
//						gameActivity.findViewById(R.id.backMenuResumeButton).setVisibility(View.VISIBLE);
						gameActivity.findViewById(R.id.backResumeButton).setVisibility(View.VISIBLE);
						gameActivity.findViewById(R.id.backMenuButton).setVisibility(View.GONE);
					}
					
				}
			});
			
			return true; 
		}
		
		return false;
//		switch(keyCode)
//		{
//			case KeyEvent.KEYCODE_DPAD_UP:
//				aimYSpeed = -5;
//				break;
//			case KeyEvent.KEYCODE_DPAD_DOWN:
//				aimYSpeed = 5;
//				break;
//			case KeyEvent.KEYCODE_DPAD_LEFT:
//				aimXSpeed = -5;
//				break;
//			case KeyEvent.KEYCODE_DPAD_RIGHT:
//				aimXSpeed = 5;
//				break;
//		}
		
	}

	public void onAccelerometerChanged(AccelerometerData accelerometerData) {
		if(accelerometer){
//			aimYSpeed = accelerometerData.getX();

//			accelerometerX.setText(accelerometerData.getX()+"");
			
//			if(accelerometerData.getX()>5)
//			{
//				aimYSpeed = 15;	
//			}
//			else if(accelerometerData.getX()>2) 
//			{
//				aimYSpeed = 10;	
//			}
//			else if(accelerometerData.getX()>0)
//			{
//				aimYSpeed = accelerometerData.getX()*3;
//			}
//			else if(accelerometerData.getX()<-5)
//			{
//				aimYSpeed = -15;	
//			}
//			else if(accelerometerData.getX()<-2) 
//			{
//				aimYSpeed = -10;	
//			}
//			else if(accelerometerData.getX()<0)
//			{
//				aimYSpeed = accelerometerData.getX()*3;
//			}
			
//			if(accelerometerData.getX()<0)
//			{
//				aimYSpeed = 8*-1;
//			}
//			else if(accelerometerData.getX()<3.5f)
//			{
//				aimYSpeed = (8-accelerometerData.getX()) *-1;
//			}
//			else if(accelerometerData.getX()<4)
//			{
//				aimYSpeed = (4-accelerometerData.getX()) *-1;
//			}
//			else if(accelerometerData.getX()<5f)
//			{
//				aimYSpeed = (5f-accelerometerData.getX());
//			}
//			else if(accelerometerData.getX()<6)
//			{
//				aimYSpeed = 10.5f-accelerometerData.getX();
//			}
//			if(accelerometerData.getX()>6)
//			{
//				aimYSpeed = 10.0f;
//			}
			
			aimYSpeed = (accelerometerData.getX()-5)*2;
			if(aimYSpeed>0)aimYSpeed*=1.5f;
			
			aimXSpeed = accelerometerData.getY()*3;
		}
	}

	public void updateXYSpeed(float xSpeed, float ySpeed)
	{
		this.aimXSpeed = xSpeed;
		this.aimYSpeed = ySpeed;
	}
	
	public void updateShellsIndicator()
	{
		float shellOffset = 0;
		
		while (shells<shellSprites.size()) {
			gameScene.getLayer(2).removeEntity(shellSprites.get(0));
			shellSprites.remove(0);
		}
		
		for(int i=0;i<shells;i++)
		{
			Sprite shell;
			if(i+1>shellSprites.size()){
				shell = new Sprite(DuckGame.shell.getWidth()/2 + shellOffset,DuckGame.CAMERA_HEIGHT-DuckGame.shell.getHeight()*1.5f,DuckGame.shell.getWidth(),DuckGame.shell.getHeight(),DuckGame.shell);
				gameScene.getLayer(2).addEntity(shell);				
				shellSprites.add(shell);
			}
			else
			{
				shell = shellSprites.get(i);
				shell.setPosition(DuckGame.shell.getWidth()/2 + shellOffset, shell.getY());
			}
			
			shellOffset += shell.getWidth();
		}
		
		scoreText.setText("" + score.intValue());
		
		ducksText.setText(ducksKilled + "/" + ducksLeft);
		ducksText.setPosition(DuckGame.CAMERA_WIDTH-ducksText.getWidth()*1.2f, 5);
		
		currLvlText.setText("Lvl."+currentLevel);
		currLvlText.setPosition(DuckGame.CAMERA_WIDTH-currLvlText.getWidth()*1.2f, ducksText.getY()+ducksText.getHeight()+5);
	}
	
	public void updateCrosshair()
	{
		aim.setPosition(aim.getX() + aimXSpeed, aim.getY()+aimYSpeed);
		
		if(aim.getY()+aim.getHeight()/2>grass.getY())
		{
			aim.setPosition(aim.getX(),grass.getY()-aim.getHeight()/2);
		}
		else if(aim.getY()+aim.getHeight()/2<0)
		{
			aim.setPosition(aim.getX(),0-aim.getHeight()/2);
		}
		
		if(aim.getX()+aim.getWidth()/2>DuckGame.CAMERA_WIDTH)
		{
			aim.setPosition(DuckGame.CAMERA_WIDTH-aim.getWidth()/2,aim.getY());
		}
		else if(aim.getX()+aim.getWidth()/2<0)
		{
			aim.setPosition(0-aim.getWidth()/2,aim.getY());
		}
		
		//move barrel au/down in percentage to aim position 
		//aimY * (barrel.height*0.2f)/grass.getY/*gameheight-grass height*/
		float barrelYOffset = aim.getY()*(barrel.getHeight()*0.2f)/grass.getY();
		
		barrel.setPosition(barrel.getX(), 
				DuckGame.CAMERA_HEIGHT-barrel.getHeight()/2/*default position*/
				+barrelYOffset);
		//rotate barrel
		Vector2 src = new Vector2(barrel.getX()+barrel.getWidth()/2,barrel.getY()+barrel.getHeight()/2);
		Vector2 dst = new Vector2(aim.getX()+aim.getWidth()/2, aim.getY()+aim.getHeight()/2);
		
		
		float angle = (float) (Math.atan2(src.y-dst.y,src.x-dst.x)*180/Math.PI-90);
		barrel.setRotation(angle);
		
		//barrel shoot flash
		flash.setPosition(barrel.getX()+barrel.getWidth()/2-flash.getWidth()/2, barrel.getY()-flash.getHeight()/2);
		flash.setRotationCenter(/*DuckGame.CAMERA_WIDTH/2*/flash.getWidth()/2, barrel.getHeight()-flash.getHeight());
		flash.setRotation(barrel.getRotation());
		
		if(flash.getAlpha()>0)
		{
			flash.setAlpha(flash.getAlpha()-0.4f);
			if(flash.getAlpha()<0)
			{
				flash.setAlpha(0);
			}
		}
		
		if(shot.getAlpha()>0)
		{
			shot.setAlpha(shot.getAlpha()-0.3f);
			if(shot.getAlpha()<0)
			{
				shot.setAlpha(0);
			}
		}
		
		if(nextLevelMsg.getAlpha()>0)
		{
			nextLevelMsg.setAlpha(nextLevelMsg.getAlpha()-0.025f);
			if(nextLevelMsg.getAlpha()<0)
			{
				nextLevelMsg.setAlpha(0);
			}
		}
	}
	
	public void updateFeathers() 
	{
		//should help with concurrent modification exception
		if(feathersBuffer.size()>0)
		{
			feathers.addAll(feathersBuffer);
			feathersBuffer.clear();
		}
		
		if(feathers.size()>0)
		{
			for(Feather f : feathers)//done 
			{
				if(f!=null)
				{
					f.update();
					//move to delete cache
					if(f.isDead())
					{
						feathersToRemove.add(f);
					}	
				}				
			}	
		}
		
		
		//remove dead feathers
		if(feathersToRemove.size()>0){
			for(Feather f : feathersToRemove)
			{
				feathers.remove(f);
				//remove from feather layer
				gameScene.getLayer(1).removeEntity(f);
			}
			feathersToRemove.clear();
		}
	}
	
	public void nextLevel(boolean restart)
	{
		ducksKilled = 0;
		ducksCreated = 0;
		shells =3;
		
		aimXSpeed = aimYSpeed = 0;
		
		if(restart)
		{
			birdSpeed=1f;
			currentLevel = 1;	
			score = 0.0;
			
			barrel.setVisible(true);
			aim.setVisible(true);
			flash.setVisible(true);
			shot.setVisible(true);
			currLvlText.setVisible(true);
			ducksText.setVisible(true);
			scoreLabel.setVisible(true);
			
			if(accelerometer)shootbutton.setVisible(true);
			
			scoreText.setVisible(true);
			for(Sprite shell:shellSprites)
			{
				shell.setVisible(true);
			}
			aim.setPosition(DuckGame.CAMERA_WIDTH/2-DuckGame.aim.getWidth()/2,DuckGame.CAMERA_HEIGHT/2-DuckGame.aim.getHeight()/2);
		}
		else
		{
			birdSpeed*=1.11f;
			currentLevel++;
			nextLevelMsg.setAlpha(1.0f);
			
		}
		
		//show admob ads
//		showHideAdmobAds(true);
	}
	
//	public void showHideAdmobAds(boolean show)
//	{
//		Log.i("Prepare to show/hide ads", show+"");
//		if(show)
//		{
//			showAds = true;
//			adTimeLeft = System.currentTimeMillis() + 45*1000;		
//			
//			gameActivity.runOnUiThread(new Thread()
//			{
//				@Override
//				public void run() {
//					final AdView ad = (AdView)gameActivity.findViewById(R.id.adView);
//
//					ad.setVisibility(View.VISIBLE);
//					ad.requestFreshAd();
//
//					Log.i("Ad status", "[show]");
//				}
//			});
//
//			
//		}
//		else
//		{
//			gameActivity.runOnUiThread(new Thread(){
//				@Override
//				public void run() {
//					final AdView ad = (AdView)gameActivity.findViewById(R.id.adView);
//
//					ad.setVisibility(View.GONE);
//					
//					Log.i("Ad status", "[hide]");
//				}});
//		}
//	}

	public void goToGameOrMenu(boolean returnToGame) {
		
		if(returnToGame)
		{
			GAME_STATE = STATE_RUN;
			nextLevel(true);
			paused = false;
		}
		else
		{
			barrel.setVisible(false);
			aim.setVisible(false);
			flash.setVisible(false);
			shot.setVisible(false);
			currLvlText.setVisible(false);
			ducksText.setVisible(false);
			scoreLabel.setVisible(false);
			shootbutton.setVisible(false);	
			scoreText.setVisible(false);
			for(Sprite shell:shellSprites)
			{
				shell.setVisible(false);
			}
			
			logo.setVisible(true);
			startButton.setVisible(true);
			settingsButton.setVisible(true);
			scoresButton.setVisible(true);
			
			
			
			
			GAME_STATE = STATE_MENU;
		}
		
	}
	
	public Double getScore() {
		return score;
	}
	
	public void setLoadComplete(boolean loadComplete) {
		this.loadComplete = loadComplete;
	}

	public void onTouch(TouchEvent sceneTouchEvent) {
		if(sceneTouchEvent.getAction()==MotionEvent.ACTION_UP && touchScreen && shells>0 && !paused)
		{
			aim.setPosition(sceneTouchEvent.getX()-aim.getWidth()/2,sceneTouchEvent.getY()-aim.getHeight()/2);
			makeShoot();
		}
		else if(sceneTouchEvent.getAction()==MotionEvent.ACTION_MOVE && touchScreen && !paused)
		{
			aim.setPosition(sceneTouchEvent.getX()-aim.getWidth()/2,sceneTouchEvent.getY()-aim.getHeight()/2);
		}
	}
	
	public void pauseGame()
	{
		paused = true;
		startPauseTime = System.currentTimeMillis();
	}
	
	public void unPauseGame()
	{
		paused = false;
		for(Bird bird: ducks)
		{
			bird.addPauseTime(System.currentTimeMillis()- startPauseTime);
		}
	}
	
	public int getGAME_STATE() {
		return GAME_STATE;
	}
	
	public boolean isPaused() {
		return paused;
	}
}
