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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UsersController;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.agent.ManageBuddiesTask;
import com.scoreloop.client.android.ui.component.agent.ManageBuddiesTask.ManageBuddiesContinuation;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.ExpandableListItem;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class UserListActivity extends ComponentListActivity<BaseListItem> {

	private enum RequestType {
		LOAD_BUDDIES, LOAD_RECOMMENDATIONS, NONE
	}

	private BaseListItem	_addBuddiesListItem;
	private List<User>		_buddies;
	private List<User>		_buddiesPlaying;
	private BaseListItem	_matchBuddyListItem;
	private RequestType		_requestType	= RequestType.NONE;
	private BaseListItem	_seeMoreListItem;
	private boolean			_showSeeMore	= true;
	private UserController	_userController;
	private UsersController	_usersController;

	private void addUsers(final BaseListAdapter<BaseListItem> adapter, final List<User> users, final Boolean playsSessionGame,
			final boolean showSeeMore) {
		int i = 0;
		for (final User user : users) {
			if (showSeeMore && (++i > ExpandableListItem.COLLAPSED_LIMIT)) {
				adapter.add(getSeeMoreListItem());
				return;
			}
			adapter.add(new UserListItem(this, getResources().getDrawable(R.drawable.sl_icon_user), user, playsSessionGame));
		}
	}

	private BaseListItem getAddBuddiesListItem() {
		if (_addBuddiesListItem == null) {
			_addBuddiesListItem = new UserAddBuddiesListItem(this);
		}
		return _addBuddiesListItem;
	}

	private BaseListItem getMatchBuddiesListItem() {
		if (_matchBuddyListItem == null) {
			_matchBuddyListItem = new UserFindMatchListItem(this);
		}
		return _matchBuddyListItem;
	}

	private BaseListItem getSeeMoreListItem() {
		if (_seeMoreListItem == null) {
			_seeMoreListItem = new ExpandableListItem(this);
		}
		return _seeMoreListItem;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new BaseListAdapter<BaseListItem>(this));

		_userController = new UserController(getReadOnlyUserControllerObserver());
		_usersController = new UsersController(getReadOnlyUserControllerObserver());

		addObservedContentKeys(Constant.NUMBER_BUDDIES);
	}

	@Override
	public void onListItemClick(final BaseListItem item) {
		if (item == _addBuddiesListItem) {
			display(getFactory().createUserAddBuddyScreenDescription());
		} else if (item == _matchBuddyListItem) {
			setNeedsRefresh(RequestType.LOAD_RECOMMENDATIONS.ordinal(), RefreshMode.SET);
			refreshIfNeeded();
		} else if (item == _seeMoreListItem) {
			_showSeeMore = false;
			updateList();
		} else {
			final UserListItem userListItem = (UserListItem) item;
			display(getFactory().createUserDetailScreenDescription(userListItem.getTarget(), userListItem.playsSessionGame()));
		}
	}

	@Override
	public void onRefresh(final int flags) {
		_requestType = RequestType.values()[flags];

		final User user = getUser();
		final Game game = getGame();

		switch (_requestType) {
		case LOAD_BUDDIES:
			_buddiesPlaying = _buddies = null;
			_userController.setUser(user);
			showSpinnerFor(_userController);
			_userController.loadBuddies();

			showSpinnerFor(_usersController);
			_usersController.loadBuddies(user, game);
			break;

		case LOAD_RECOMMENDATIONS:
			showSpinnerFor(_usersController);
			_usersController.loadRecommendedBuddies(1);
			break;
		}
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		if (isValueChangedFor(key, Constant.NUMBER_BUDDIES, oldValue, newValue)) {
			setNeedsRefresh(RequestType.LOAD_BUDDIES.ordinal(), RefreshMode.SET);
		}
	}

	@Override
	public void onValueSetDirty(final ValueStore valueStore, final String key) {
		retrieveContentValueFor(key, Constant.NUMBER_BUDDIES, ValueStore.RetrievalMode.NOT_DIRTY, null);
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		final RequestType requestType = _requestType;
		_requestType = RequestType.NONE;

		switch (requestType) {
		case LOAD_BUDDIES:
			if (aRequestController == _usersController) {
				_buddiesPlaying = _usersController.getUsers();
			} else if (aRequestController == _userController) {
				_buddies = _userController.getUser().getBuddyUsers();
				// we have to copy this list, otherwise bad side-effects will happen when we delete entries from it
				if (_buddies != null) {
					_buddies = new ArrayList<User>(_buddies);
				}
			}
			if ((_buddiesPlaying != null) && (_buddies != null)) {
				for (final User buddyPlaying : _buddiesPlaying) {
					for (int i = 0; i < _buddies.size(); i++) {
						final User buddy = _buddies.get(i);
						if (buddy.getIdentifier().equals(buddyPlaying.getIdentifier())) {
							_buddies.remove(i);
							break;
						}
					}
				}
				updateList();
			} else {
				_requestType = requestType; // re-establish request type when one controller response is still outstanding
			}
			break;
		case LOAD_RECOMMENDATIONS:
			if (aRequestController == _usersController) {
				final List<User> users = _usersController.getUsers();
				if (users.size() > 0) {
					showSpinner();
					ManageBuddiesTask.addBuddy(users.get(0), getManager(), new ManageBuddiesContinuation() {
						public void withAddedOrRemovedBuddies(final int count) {
							hideSpinner();
							setNeedsRefresh(RequestType.LOAD_BUDDIES.ordinal(), RefreshMode.SET);
							if (!isPaused()) {
								showToast(getResources().getString(R.string.sl_format_one_friend_added));
							}
						}
					});
				}
			}
			break;
		}
	}

	private void updateList() {
		final BaseListAdapter<BaseListItem> adapter = getBaseListAdapter();
		adapter.clear();

		final boolean isSessionUser = isSessionUser();

		if (isSessionUser) {
			adapter.add(getAddBuddiesListItem());
		}

		boolean hasBuddies = false;
		final int otherBuddiesCount = _buddies.size();
		if (_buddiesPlaying.size() > 0) {
			adapter.add(new CaptionListItem(this, null, String.format(getString(R.string.sl_format_friends_playing), getGame().getName())));
			addUsers(adapter, _buddiesPlaying, true, _showSeeMore && (otherBuddiesCount > 0));
			hasBuddies = true;
		}
		if (otherBuddiesCount > 0) {
			adapter.add(new CaptionListItem(this, null, getString(R.string.sl_friends)));
			addUsers(adapter, _buddies, false, false);
			hasBuddies = true;
		}
		if (!hasBuddies) {
			if (isSessionUser) {
				adapter.add(getMatchBuddiesListItem());
			} else {
				adapter.add(new CaptionListItem(this, null, getString(R.string.sl_no_friends)));
			}
		}
	}
}
