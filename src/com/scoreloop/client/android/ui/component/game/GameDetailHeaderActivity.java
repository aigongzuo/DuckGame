/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.ui.component.game;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.ui.component.base.ComponentHeaderActivity;
import com.scoreloop.client.android.ui.component.base.PackageManager;
import com.scoreloop.client.android.ui.util.ImageDownloader;

public class GameDetailHeaderActivity extends ComponentHeaderActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.sl_header_game);
		final Game game = getGame();
		if(game.getPackageNames() != null) {
			if(PackageManager.isGameInstalled(this, game)) {
				Button launch = getControlButton();
				launch.setText(getString(R.string.sl_launch));
				launch.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						PackageManager.launchGame(GameDetailHeaderActivity.this, game);
					}
				});
				showControlButton(launch);
			}
			else {
				Button get = getControlButton();
				get.setText(getString(R.string.sl_get));
				get.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						hideControlButton();
						PackageManager.installGame(GameDetailHeaderActivity.this, game);
					}
				});
				showControlButton(get);
			}
		}
		else {
			hideControlButton();
		}
		ImageDownloader.downloadImage(game.getImageUrl(), getResources().getDrawable(R.drawable.sl_icon_games), getImageView());
		setTitle(game.getName());
		setSubTitle(game.getPublisherName());
	}

	private Button getControlButton() {
		return (Button)getLayoutInflater().inflate(R.layout.sl_control_button, null);
	}

	private ViewGroup hideControlButton() {
		ViewGroup viewGroup = (ViewGroup)findViewById(R.id.sl_control_header);
		if(viewGroup != null) {
			viewGroup.removeAllViews();
		}
		return viewGroup;
	}
	
	private void showControlButton(Button button) {
		ViewGroup viewGroup = hideControlButton();
		if(viewGroup != null) {
			viewGroup.addView(button);
		}
	}
}
