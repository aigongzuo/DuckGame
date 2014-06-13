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
import com.scoreloop.client.android.core.controller.ChallengeController;
import com.scoreloop.client.android.core.controller.ChallengeControllerObserver;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.ui.component.agent.UserDetailsAgent;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.component.challenge.ChallengeControlsListItem.OnControlObserver;
import com.scoreloop.client.android.ui.framework.BaseDialog;
import com.scoreloop.client.android.ui.framework.TextButtonDialog;
import com.scoreloop.client.android.ui.framework.ValueStore;
import com.scoreloop.client.android.ui.framework.BaseDialog.OnActionListener;

public class ChallengeAcceptListActivity extends ChallengeActionListActivity implements ChallengeControllerObserver, OnActionListener, OnControlObserver {

	private Challenge						_challenge;
	private ChallengeParticipantsListItem	_challengeParticipantsListItem;
	private boolean							_isBackAllowed;
	private ValueStore						_opponentValueStore;

	public void challengeControllerDidFailOnInsufficientBalance(final ChallengeController challengeController) {
		TextButtonDialog dialog = new TextButtonDialog(this);
		dialog.setText(getResources().getString(R.string.sl_error_message_challenge_balance));
		dialog.setOnActionListener(this);
		showDialogSafe(dialog);
	}

	public void challengeControllerDidFailToAcceptChallenge(final ChallengeController challengeController) {
		TextButtonDialog dialog = new TextButtonDialog(this);
		dialog.setText(getResources().getString(R.string.sl_error_message_challenge_accept));
		dialog.setOnActionListener(this);
		showDialogSafe(dialog);
	}

	public void challengeControllerDidFailToRejectChallenge(final ChallengeController challengeController) {
		TextButtonDialog dialog = new TextButtonDialog(this);
		dialog.setText(getResources().getString(R.string.sl_error_message_challenge_reject));
		dialog.setOnActionListener(this);
		showDialogSafe(dialog);
	}

	@Override
	CaptionListItem getCaptionListItem() {
		return new CaptionListItem(ChallengeAcceptListActivity.this, null, getString(R.string.sl_accept_challenge));
	}

	@Override
	ChallengeControlsListItem getChallengeControlsListItem() {
		return new ChallengeControlsListItem(this, _challenge, this);
	}

	@Override
	ChallengeParticipantsListItem getChallengeParticipantsListItem() {
		if (_challengeParticipantsListItem == null) {
			final String contenderName = _challenge.getContender().getDisplayName();
			final String contestantName = getUser().getDisplayName();
			_challengeParticipantsListItem = new ChallengeParticipantsListItem(this, contenderName, null, contestantName, null);
		}
		return _challengeParticipantsListItem;
	}

	@Override
	ChallengeSettingsListItem getChallengeStakeAndModeListItem() {
		return new ChallengeSettingsListItem(this, _challenge);
	}

	@Override
	protected boolean isBackAllowed() {
		return _isBackAllowed;
	}

	public void onAction(BaseDialog dialog, final int action) {
		dialog.dismiss();
		displayPrevious();
	}

	public void onControl1() {
		if (challengeGamePlayAllowed()) {
			_isBackAllowed = false;
			_challenge.setContestant(getUser());
			final ChallengeController challengeController = new ChallengeController(this);
			showSpinnerFor(challengeController);
			challengeController.setChallenge(_challenge);
			challengeController.acceptChallenge();
		}
	}

	public void onControl2() {
		_challenge.setContestant(getUser());
		final ChallengeController challengeController = new ChallengeController(this);
		showSpinnerFor(challengeController);
		challengeController.setChallenge(_challenge);
		challengeController.rejectChallenge();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addObservedContentKeys(Constant.NUMBER_CHALLENGES_WON);

		_challenge = getActivityArguments().getValue(Constant.CHALLENGE, null); // assert challenge != null

		_opponentValueStore = new ValueStore();
		_opponentValueStore.putValue(Constant.USER, _challenge.getContender());
		_opponentValueStore.addObserver(Constant.NUMBER_CHALLENGES_WON, this);
		_opponentValueStore.addValueSources(new UserDetailsAgent());
		setNeedsRefresh();

		initAdapter();
	}

	@Override
	public void onRefresh(final int flags) {
		_opponentValueStore.retrieveValue(Constant.NUMBER_CHALLENGES_WON, ValueStore.RetrievalMode.NOT_DIRTY, null);
	}

	@Override
	public void onResume() {
		super.onResume();

		_isBackAllowed = true;
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		if (valueStore == _opponentValueStore) {
			if (isValueChangedFor(key, Constant.NUMBER_CHALLENGES_WON, oldValue, newValue)) {
				getChallengeParticipantsListItem().setContenderStats(StringFormatter.getChallengesSubTitle(this, _opponentValueStore));
				getBaseListAdapter().notifyDataSetChanged();
			}

		} else {
			if (isValueChangedFor(key, Constant.NUMBER_CHALLENGES_WON, oldValue, newValue)) {
				getChallengeParticipantsListItem().setContestantStats(StringFormatter.getChallengesSubTitle(this, valueStore));
				getBaseListAdapter().notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onValueSetDirty(final ValueStore valueStore, final String key) {
		if (valueStore == _opponentValueStore) {
			_opponentValueStore.retrieveValue(Constant.NUMBER_CHALLENGES_WON, ValueStore.RetrievalMode.NOT_DIRTY, null);
		} else {
			retrieveContentValueFor(key, Constant.NUMBER_CHALLENGES_WON, ValueStore.RetrievalMode.NOT_DIRTY, null);
		}
	}

	@Override
	protected void requestControllerDidFailSafe(final RequestController aRequestController, final Exception anException) {
		_isBackAllowed = true;
		showDialogForExceptionSafe(anException);
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		final Challenge challenge = ((ChallengeController) aRequestController).getChallenge();
		if (challenge.isAccepted()) {
			finishDisplay();
			getManager().startGamePlay(_challenge.getMode(), _challenge);
		} else if (challenge.isRejected()) {
			displayPrevious();
		} else {
			throw new IllegalStateException("this should not happen - illegal state of the accepted/rejected challenge");
		}
	}
}
