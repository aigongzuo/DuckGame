package com.phundroid.duck;

import java.util.Random;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class Feather extends Sprite{

	private Random random =new Random();
	private long lifeTime;
	private long creationTime;
//	private int rotation = 0;
//	private int rotationSide = 1;
	private boolean dead = false;
	
	public Feather(float px, float py, float width, float height,
			TextureRegion textureRegion) {
		super(px, py, width, height, textureRegion);
		
		lifeTime = (2+random.nextInt(7))*1000; //default 2 seconds and + 0-10 seconds, to make hide feathers now all in one time
		creationTime = System.currentTimeMillis();
//		if(random.nextBoolean())
//		{
//			rotationSide = 1;
//		}
//		else
//		{
//			rotationSide = -1;
//		}
	}
	
	public void update()
	{
		//call current time for every feather each loop, 
		//may be for optimize move it in one main loop 
		//of checking feathers, to make one call for all feathers
		if(System.currentTimeMillis()-creationTime<lifeTime)
		{
//			setSize(getWidth()*0.99f, getHeight()*0.99f);
//			rotation += 5;
//			setRotation(rotationSide*rotation);
			
			if(random.nextBoolean())
			{
				setPosition(getX()+random.nextInt(10), getY()+1);
			}
			else
			{
				setPosition(getX()+random.nextInt(10)*-1, getY()+1);
			}
		}
		else
		{
			dead  = true;
		}
	}

	public boolean isDead() {
		return dead;
	}
}
