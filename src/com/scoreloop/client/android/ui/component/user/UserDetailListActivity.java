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

package com.scoreloop.client.android.ui.component.user;

import android.os.Bundle;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.controller.MessageController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.component.base.Configuration;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.Factory;
import com.scoreloop.client.android.ui.component.base.StandardListItem;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class UserDetailListActivity extends ComponentListActivity<BaseListItem> {

	// if user-plays-game argument is true, then we display the Game section
	// if arg is null, we have to determine whether user plays game
	// all the above is only true if the game is the session game and the configurations are enabled

	private static enum GameSectionDisplayOption {
		HIDE, RECOMMEND, SHOW, UNKNOWN
	}

	private UserDetailListItem			_achievementsListItem;
	private UserDetailListItem			_buddiesListItem;
	private BaseListItem				_challengesListItem;
	private GameSectionDisplayOption	_gameSectionDisplayOption	= GameSectionDisplayOption.UNKNOWN;
	private UserDetailListItem			_gamesListItem;
	private BaseListItem				_recommendListItem;

	private UserDetailListItem getAchievementsListItem() {
		if (_achievementsListItem == null) {
			_achievementsListItem = new UserDetailListItem(this, getResources().getDrawable(R.drawable.sl_icon_achievements),
					getString(R.string.sl_achievements), StringFormatter.getAchievementsSubTitle(this, getContentValues(), false));
		}
		return _achievementsListItem;
	}

	private UserDetailListItem getBuddiesListItem() {
		if (_buddiesListItem == null) {
			_buddiesListItem = new UserDetailListItem(this, getResources().getDrawable(R.drawable.sl_icon_friends),
					getString(R.string.sl_friends), StringFormatter.getBuddiesSubTitle(this, getContentValues()));
		}
		return _buddiesListItem;
	}

	private BaseListItem getChallengesListItem() {
		if (_challengesListItem == null) {
			_challengesListItem = new StandardListItem<Void>(this, getResources().getDrawable(R.drawable.sl_icon_challenges),
					getString(R.string.sl_challenges), getString(R.string.sl_challenge_this_friend), null);
		}
		return _challengesListItem;
	}

	private CaptionListItem getCommunityCaptionListItem() {
		return new CaptionListItem(this, null, getString(R.string.sl_community));
	}

	private CaptionListItem getGameCaptionListItem() {
		return new CaptionListItem(this, null, getGame().getName());
	}

	private UserDetailListItem getGamesListItem() {
		if (_gamesListItem == null) {
			_gamesListItem = new UserDetailListItem(this, getResources().getDrawable(R.drawable.sl_icon_games),
					getString(R.string.sl_games), StringFormatter.getGamesSubTitle(this, getContentValues()));
		}
		return _gamesListItem;
	}

	private BaseListItem getRecommendListItem() {
		if (_recommendListItem == null) {
			final User user = getUser();
			final Game game = getGame();

			final String title = String.format(getString(R.string.sl_format_recommend_title), game.getName());
			final String subtitle = String.format(getString(R.string.sl_format_recommend_subtitle), user.getDisplayName());

			_recommendListItem = new StandardListItem<Void>(this, getResources().getDrawable(R.drawable.sl_icon_recommend), title,
					subtitle, null);
		}
		return _recommendListItem;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new BaseListAdapter<BaseListItem>(this));

		addObservedContentKeys(Constant.NUMBER_BUDDIES, Constant.NUMBER_GAMES, Constant.NUMBER_ACHIEVEMENTS);

		setNeedsRefresh();
	}

	@Override
	public void onListItemClick(final BaseListItem item) {
		final User user = getUser();
		final Factory factory = getFactory();

		if (item == getRecommendListItem()) {
			postRecommendation();
		} else if (item == getAchievementsListItem()) {
			display(factory.createAchievementScreenDescription(user));
		} else if (item == getChallengesListItem()) {
			display(factory.createChallengeCreateScreenDescription(user, null));
		} else if (item == getBuddiesListItem()) {
			display(factory.createUserScreenDescription(user));
		} else if (item == this.getGamesListItem()) {
			display(factory.createUserGameScreenDescription(user, Constant.GAME_MODE_USER));
		}
	}

	@Override
	public void onRefresh(final int flags) {
		if (_gameSectionDisplayOption == GameSectionDisplayOption.UNKNOWN) {

			if (isSessionGame()) {
				final Boolean userPlaysGame = getActivityArguments().getValue(Constant.USER_PLAYS_SESSION_GAME);
				if (userPlaysGame == null) {
					_gameSectionDisplayOption = GameSectionDisplayOption.UNKNOWN;
				} else if (userPlaysGame) {
					_gameSectionDisplayOption = GameSectionDisplayOption.SHOW;
				} else {
					_gameSectionDisplayOption = GameSectionDisplayOption.RECOMMEND;
				}
			} else {
				_gameSectionDisplayOption = GameSectionDisplayOption.HIDE;
			}
			updateList();
		}
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		final ValueStore contentValues = getContentValues();
		if (isValueChangedFor(key, Constant.NUMBER_BUDDIES, oldValue, newValue)) {
			getBuddiesListItem().setSubTitle(StringFormatter.getBuddiesSubTitle(this, contentValues));
			getBaseListAdapter().notifyDataSetChanged();
		} else if (isValueChangedFor(key, Constant.NUMBER_GAMES, oldValue, newValue)) {
			getGamesListItem().setSubTitle(StringFormatter.getGamesSubTitle(this, contentValues));
			getBaseListAdapter().notifyDataSetChanged();
		} else if (isValueChangedFor(key, Constant.NUMBER_ACHIEVEMENTS, oldValue, newValue)) {
			getAchievementsListItem().setSubTitle(StringFormatter.getAchievementsSubTitle(this, contentValues, false));
			getBaseListAdapter().notifyDataSetChanged();
		}
	}

	@Override
	public void onValueSetDirty(final ValueStore valueStore, final String key) {
		retrieveContentValueFor(key, Constant.NUMBER_BUDDIES, ValueStore.RetrievalMode.NOT_DIRTY, null);
		retrieveContentValueFor(key, Constant.NUMBER_GAMES, ValueStore.RetrievalMode.NOT_DIRTY, null);

		final Configuration configuration = getConfiguration();
		if (configuration.isFeatureEnabled(Configuration.Feature.ACHIEVEMENT)) {
			retrieveContentValueFor(key, Constant.NUMBER_ACHIEVEMENTS, ValueStore.RetrievalMode.NOT_DIRTY, null);
		}
	}

	private void postRecommendation() {
		final MessageController controller = new MessageController(getRequestControllerObserver());

		controller.setTarget(getGame());
		controller.setMessageType(MessageController.TYPE_RECOMMENDATION);
		controller.addReceiverWithUsers(MessageController.RECEIVER_USER, getUser());

		if (controller.isSubmitAllowed()) {
			showSpinnerFor(controller);
			controller.submitMessage();
		}
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		showToast(getString(R.string.sl_recommend_sent));
	}

	private void updateList() {
		final BaseListAdapter<BaseListItem> adapter = getBaseListAdapter();
		adapter.clear();

		final Configuration configuration = getConfiguration();

		switch (_gameSectionDisplayOption) {
		case SHOW:
			final boolean showAchievements = configuration.isFeatureEnabled(Configuration.Feature.ACHIEVEMENT);
			final boolean showChallenges = configuration.isFeatureEnabled(Configuration.Feature.CHALLENGE);

			if (showAchievements || showChallenges) {
				adapter.add(getGameCaptionListItem());
				if (showAchievements) {
					adapter.add(getAchievementsListItem());
				}
				if (showChallenges) {
					adapter.add(getChallengesListItem());
				}
			}
			break;

		case RECOMMEND:
			adapter.add(getGameCaptionListItem());
			adapter.add(getRecommendListItem());
			break;
		}

		adapter.add(getCommunityCaptionListItem());
		adapter.add(getBuddiesListItem());
		adapter.add(getGamesListItem());
	}
}
