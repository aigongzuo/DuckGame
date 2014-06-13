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

import java.util.List;

import android.os.Bundle;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.controller.GamesController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.PagingListAdapter;

public class GameListActivity extends ComponentListActivity<BaseListItem> implements RequestControllerObserver {

	private static final int	RANGE_LENGTH	= 25;

	private GamesController		_gamesController;
	private int					_mode;

	private void addGames(final BaseListAdapter<BaseListItem> adapter, final List<Game> games) {
		for (final Game game : games) {
			adapter.add(new GameListItem(this, getResources().getDrawable(R.drawable.sl_icon_games_loading), game));
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new PagingListAdapter<BaseListItem>(this));
		_mode = getActivityArguments().<Integer>getValue(Constant.MODE);
		_gamesController = new GamesController(this);
		_gamesController.setRangeLength(RANGE_LENGTH);
		setNeedsRefresh();
	}

	private void onGames(List<Game> games) {
		final BaseListAdapter<BaseListItem> adapter = getBaseListAdapter();
		adapter.clear();
		addGames(adapter, games);
		int id = 0;
		if (games.size() == 0) {
			id = R.string.sl_no_games;
		} else {
			switch (_mode) {
			case Constant.GAME_MODE_USER:
				id = isSessionUser() ? R.string.sl_my_games : R.string.sl_games;
				break;
			case Constant.GAME_MODE_POPULAR:
				id = R.string.sl_popular_games;
				break;
			case Constant.GAME_MODE_NEW:
				id = R.string.sl_new_games;
				break;
			case Constant.GAME_MODE_BUDDIES:
				id = R.string.sl_friends_games;
				break;
			default:
				id = R.string.sl_games;
				break;
			}
		}
		adapter.insert(new CaptionListItem(this, null, getString(id)), 0);
	}

	@Override
	public void onListItemClick(final BaseListItem item) {
		if (item.getType() == Constant.LIST_ITEM_TYPE_GAME) {
			display(getFactory().createGameDetailScreenDescription(((GameListItem) item).getTarget()));
		}
	}

	@Override
	public void onRefresh(final int flags) {
		switch(_mode) {
		case Constant.GAME_MODE_USER:
			onRefreshUser();
			break;
		case Constant.GAME_MODE_POPULAR:
			onRefreshPopular();
			break;
		case Constant.GAME_MODE_NEW:
			onRefreshNew();
			break;
		case Constant.GAME_MODE_BUDDIES:
			onRefreshBuddies();
			break;
		}
	}

	private void onRefreshBuddies() {
		showSpinnerFor(_gamesController);
		_gamesController.loadRangeForBuddies();
	}

	private void onRefreshNew() {
		showSpinnerFor(_gamesController);
		_gamesController.loadRangeForNew();
	}

	private void onRefreshPopular() {
		showSpinnerFor(_gamesController);
		_gamesController.loadRangeForPopular();
	}

	private void onRefreshUser() {
		showSpinnerFor(_gamesController);
		_gamesController.loadRangeForUser(getUser());
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		if (aRequestController == _gamesController) {
			onGames(_gamesController.getGames());
		}
	}
}
