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

package com.scoreloop.client.android.ui.component.challenge;

import android.os.Bundle;

import com.phundroid.duck.R;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.TextButtonDialog;
import com.scoreloop.client.android.ui.framework.BaseDialog.OnActionListener;

public abstract class ChallengeActionListActivity extends ComponentListActivity<BaseListItem> implements OnActionListener {
	
	abstract CaptionListItem getCaptionListItem();

	abstract ChallengeControlsListItem getChallengeControlsListItem();

	abstract ChallengeParticipantsListItem getChallengeParticipantsListItem();

	abstract ChallengeSettingsListItem getChallengeStakeAndModeListItem();

	void initAdapter() {
		final BaseListAdapter<BaseListItem> adapter = new BaseListAdapter<BaseListItem>(this);
		adapter.add(getCaptionListItem());
		adapter.add(getChallengeParticipantsListItem());
		adapter.add(getChallengeStakeAndModeListItem());
		adapter.add(getChallengeControlsListItem());
		setListAdapter(adapter);
	}
	
	boolean challengeGamePlayAllowed() {
		if (getManager().isChallengeOngoing()) {
			TextButtonDialog dialog = new TextButtonDialog(this);
			dialog.setText(getResources().getString(R.string.sl_error_message_challenge_ongoing));
			dialog.setOnActionListener(this);
			showDialogSafe(dialog);
			return false;
		} else if (!getManager().canStartGamePlay()) {
			TextButtonDialog dialog = new TextButtonDialog(this);
			dialog.setText(getResources().getString(R.string.sl_error_message_challenge_game_not_ready));
			dialog.setOnActionListener(this);
			showDialogSafe(dialog);
			return false;
		} else {
			return true;
		}

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
