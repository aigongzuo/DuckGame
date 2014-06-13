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

package com.scoreloop.client.android.ui.component.base;

import com.scoreloop.client.android.core.controller.RequestCancelledException;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.RequestControllerObserver;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UserControllerObserver;
import com.scoreloop.client.android.core.model.Game;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.framework.BaseActivity;
import com.scoreloop.client.android.ui.framework.ValueStore;
import com.scoreloop.client.android.ui.framework.ValueStore.RetrievalMode;

public abstract class ComponentActivity extends BaseActivity implements ComponentActivityHooks {

	private class ReadOnlyRequestControllerObserver extends StandardRequestControllerObserver implements UserControllerObserver {

		public void userControllerDidFailOnEmailAlreadyTaken(final UserController controller) {
			hideSpinnerFor(controller);
		}

		public void userControllerDidFailOnInvalidEmailFormat(final UserController controller) {
			hideSpinnerFor(controller);
		}

		public void userControllerDidFailOnUsernameAlreadyTaken(final UserController controller) {
			hideSpinnerFor(controller);
		}
	}

	private class StandardRequestControllerObserver implements RequestControllerObserver {

		public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
			ComponentActivity.this.requestControllerDidFail(aRequestController, anException);
		}

		public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
			ComponentActivity.this.requestControllerDidReceiveResponse(aRequestController);
		}
	}

	public static boolean isValueChangedFor(final String aKey, final String theKey, final Object oldValue, final Object newValue) {
		return aKey.equals(theKey) && ((newValue != null) && !newValue.equals(oldValue));
	}

	private UserControllerObserver		_readOnlyUserControllerObserver;
	private RequestControllerObserver	_requestControllerObserver;

	public void addObservedContentKeys(final String... keys) {
		final String[] contentKeys = new String[keys.length];
		for (int i = 0; i < keys.length; ++i) {
			contentKeys[i] = Constant.asContentKey(keys[i]);
		}
		addObservedKeys(contentKeys);
	}

	public Configuration getConfiguration() {
		return getScreenValues().getValue(Constant.CONFIGURATION);
	}

	public ValueStore getContentValues() {
		return getScreenValues().getValue(Constant.CONTENT_VALUES);
	}
	
	public ValueStore getSessionUserValues() {
		return getScreenValues().getValue(Constant.SESSION_USER_VALUES);
	}

	public Factory getFactory() {
		return getScreenValues().getValue(Constant.FACTORY);
	}

	public Game getGame() {
		return getContentValues().getValue(Constant.GAME);
	}

	public Manager getManager() {
		return getScreenValues().getValue(Constant.MANAGER);
	}

	public int getModeForPosition(final int position) {
		final Game game = getGame();
		return position + game.getMinMode();
	}

	public String getModeString(final int mode) {
		return getResources().getStringArray(getConfiguration().getModesResId())[getPositionForMode(mode)].toString();
	}

	public int getPositionForMode(final int mode) {
		final Game game = getGame();
		return mode - game.getMinMode();
	}

	protected UserControllerObserver getReadOnlyUserControllerObserver() {
		if (_readOnlyUserControllerObserver == null) {
			_readOnlyUserControllerObserver = new ReadOnlyRequestControllerObserver();
		}
		return _readOnlyUserControllerObserver;
	}

	protected RequestControllerObserver getRequestControllerObserver() {
		if (_requestControllerObserver == null) {
			_requestControllerObserver = new StandardRequestControllerObserver();
		}
		return _requestControllerObserver;
	}

	public Session getSession() {
		return Session.getCurrentSession();
	}

	public User getSessionUser() {
		return getSession().getUser();
	}

	public User getUser() {
		return getContentValues().getValue(Constant.USER);
	}

	public boolean isSessionGame() {
		final Game game = getGame();
		return game != null ? game.equals(getSession().getGame()) : false;
	}

	public boolean isSessionUser() {
		final User user = getUser();
		return user != null ? user.ownsSession(getSession()) : false;
	}

	public final void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
		if (!(anException instanceof RequestCancelledException)) {
			hideSpinnerFor(aRequestController);
			if (!isPaused()) {
				requestControllerDidFailSafe(aRequestController, anException);
			}
		}
	}

	protected void requestControllerDidFailSafe(final RequestController aRequestController, final Exception anException) {
		showDialogForExceptionSafe(anException);
	}

	public final void requestControllerDidReceiveResponse(final RequestController aRequestController) {
		hideSpinnerFor(aRequestController);
		if (!isPaused()) {
			requestControllerDidReceiveResponseSafe(aRequestController);
		} else {
			setNeedsRefresh();
		}
	}

	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		// intentionally empty - override in subclass
	}

	public void retrieveContentValueFor(final String aKey, final String theKey, final RetrievalMode mode, final Object argument) {
		if (aKey.equalsIgnoreCase(theKey)) {
			final ValueStore contentValues = getContentValues();
			contentValues.retrieveValue(theKey, mode, argument);
		}
	}
}
