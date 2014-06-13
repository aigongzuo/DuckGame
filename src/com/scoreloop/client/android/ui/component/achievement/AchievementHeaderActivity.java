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

package com.scoreloop.client.android.ui.component.achievement;

import android.os.Bundle;

import com.phundroid.duck.R;
import com.scoreloop.client.android.ui.component.base.ComponentHeaderActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class AchievementHeaderActivity extends ComponentHeaderActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.sl_header_default);

		getImageView().setImageDrawable(getResources().getDrawable(R.drawable.sl_header_icon_achievements));
		setCaption(getGame().getName());
		setTitle(getString(R.string.sl_achievements));

		addObservedContentKeys(Constant.NUMBER_ACHIEVEMENTS, Constant.NUMBER_AWARDS);
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		setSubTitle(StringFormatter.getAchievementsSubTitle(this, getContentValues(), true));
	}

	@Override
	public void onValueSetDirty(final ValueStore valueStore, final String key) {
		retrieveContentValueFor(key, Constant.NUMBER_ACHIEVEMENTS, ValueStore.RetrievalMode.NOT_DIRTY, null);
		retrieveContentValueFor(key, Constant.NUMBER_AWARDS, ValueStore.RetrievalMode.NOT_DIRTY, null);
	}
}
