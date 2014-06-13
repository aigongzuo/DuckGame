package com.phundroid.duck;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class Cloud extends Sprite {

	private float cloudSpeed = 0;	
	
	public Cloud(float px, float py, float width, float height,
			TextureRegion textureRegion) {
		super(px, py, width, height, textureRegion);
		
	}

	public float getCloudSpeed() {
		return cloudSpeed;
	}

	public void setCloudSpeed(float cloudSpeed) {
		this.cloudSpeed = cloudSpeed;
	}

	public void update()
	{
		float currX = getX() + cloudSpeed;
		if(currX+getWidth()<0)
		{
			currX = DuckGame.CAMERA_WIDTH;			
		}else if(currX>DuckGame.CAMERA_WIDTH) 
		{
			currX = 0-getWidth();
		}
		
		setPosition(currX, getY());
	}
}
