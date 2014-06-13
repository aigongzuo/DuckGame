package com.phundroid.duck;

import java.util.Random;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class Bird extends AnimatedSprite{
	
	private boolean falling = false;
	private boolean fallen = false;
	private boolean flying = true;
	private boolean flyAway = false;
	
	private boolean wasScared = true;
	
	private int deadRotation = 0;
	
	private float xSpeed = 1, ySpeed = 0;
	private int speed = 4;
	private Random r = new Random();
	private final long creationTime;
	private long pauseTime=0;
	private final long lifeTime = 30*1000;//30 secs 
	private boolean useSound;
	
	public Bird(float px, float py, float width, float height,
			TiledTextureRegion textureRegion, boolean useSound) {
		super(px, py, width, height, textureRegion);
	
		creationTime = System.currentTimeMillis();
		
		if(r.nextBoolean())
		{
			ySpeed = 1;
		}
		else
		{
			ySpeed = -1;
		}
		
		this.useSound = useSound;
	}
	
	public float getXSpeed() {
		return xSpeed;
	}
	public void setXSpeed(float speed) {
		xSpeed = speed;
	}

	public void update()
	{
		float currX = getX();
		float currY = getY();
	
//		>Ducks need to not only bounce of the screen but eventually fly away after a certain amount of time. Let's say 30 seconds. 
		if(System.currentTimeMillis() - creationTime-pauseTime > lifeTime && isFlying())
		{
			setFlyAway(true);
		}
		
		if(flying)
		{
			if(wasScared)
			{
				ySpeed=-ySpeed;
				wasScared = false;
			}
			
			currX = currX + speed * xSpeed;
			if(currX<0)
			{
				currX = 0;
				getTextureRegion().setFlippedHorizontal(!getTextureRegion().isFlippedHorizontal());
				xSpeed=-xSpeed;
//				currX = DuckGame.CAMERA_WIDTH;			
			}
			else if(currX+getWidth()>DuckGame.CAMERA_WIDTH) 
			{
				currX = DuckGame.CAMERA_WIDTH-getWidth();
//				currX = 0;
				getTextureRegion().setFlippedHorizontal(!getTextureRegion().isFlippedHorizontal());
				xSpeed=-xSpeed;
			}
			
			currY = currY + speed * ySpeed;
			if(currY<0)
			{
				currY = 0;
//				getTextureRegion().setFlippedHorizontal(!getTextureRegion().isFlippedHorizontal());
				ySpeed=-ySpeed;
//				currX = DuckGame.CAMERA_WIDTH;			
			}
			else if(currY+getHeight()>DuckGame.CAMERA_HEIGHT-DuckGame.grass.getHeight()) 
			{
				currY = DuckGame.CAMERA_HEIGHT-DuckGame.grass.getHeight()-getWidth();
//				currX = 0;
//				getTextureRegion().setFlippedHorizontal(!getTextureRegion().isFlippedHorizontal());
				ySpeed=-ySpeed;
			}
			
			int rotCoeff = 1;
			
			if(xSpeed>0)
			{
				rotCoeff = 1;
			}
			else
			{
				rotCoeff = -1;
			}
			
			if(ySpeed>0)
			{
				setRotation(15*rotCoeff);
			}
			else
			{
				setRotation(-15*rotCoeff);
			}
			
			
			setPosition(currX, currY);
		}
		else if(flyAway)
		{
			ySpeed =Math.abs(ySpeed)*-1;
			
			currX = currX + speed * xSpeed*2;
			currY = currY + speed * ySpeed*2;
			
			if(currX+getWidth()<0)
			{
				fallen = true;		
			}else if(currX>DuckGame.CAMERA_WIDTH) 
			{
				fallen = true;
			}
			
			if(currY+getHeight()<0)
			{
				fallen = true;
			}
			
			int rotCoeff = 1;
			if(xSpeed>0)
			{
				rotCoeff = 1;
			}
			else
			{
				rotCoeff = -1;
			}
			setRotation(-15*rotCoeff);
			setPosition(currX, currY);
		}
		else if (falling)
		{
			currX = currX + 2 * xSpeed;
			if(currX+getWidth()<0)
			{
				currX = DuckGame.CAMERA_WIDTH;			
			}else if(currX>DuckGame.CAMERA_WIDTH) 
			{
				currX = 0-getWidth();
			}
			
			currY +=10;
			
			if(currY > DuckGame.CAMERA_HEIGHT-DuckGame.grass.getHeight()*0.8f)
			{
				currY = DuckGame.CAMERA_HEIGHT-DuckGame.grass.getHeight()*0.8f;
				
				fallen = true;
				falling = false;
			}
			
			setPosition(currX, currY);
			deadRotation += 5*xSpeed;
			setRotation(deadRotation);
		}
		
	}

	public boolean isFalling() {
		return falling;
	}

	public void setFalling(boolean falling) {
		this.falling = falling;
		
		if(falling)
		{
			flying = false;
		}
	}

	public boolean isFallen() {
		return fallen;
	}

	public void setFallen(boolean fallen) {
		this.fallen = fallen;
	}

	public boolean isFlying() {
		return flying;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public void setFlyAway(boolean flyAway) {
		this.flyAway = flyAway;
		
		if(flyAway)
		{
			animate(new long[]{100,100,100,100}, 4, 7, true);
			if(useSound)DuckGame.sDuckQuack.play();
			
			flying = false;
		}
	}

	public boolean isWasScared() {
		return wasScared;
	}

	public void setWasScared(boolean wasScared) {
		this.wasScared = wasScared;
	}
	
	public float timeLeft() {
		return (System.currentTimeMillis() - creationTime-pauseTime)/1000f;
	}
	
	public float getLifeTime() {
		return lifeTime/1000f;
	}

	public long getPauseTime() {
		return pauseTime;
	}

	public void setPauseTime(long pauseTime) {
		this.pauseTime = pauseTime;
	}
	
	public void addPauseTime(long pauseTime)
	{
		this.pauseTime+=pauseTime;
	}
}
