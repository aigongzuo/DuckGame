package com.scoreloop.client.android.ui.component.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UserControllerObserver;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.Manager;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class ManageBuddiesTask implements UserControllerObserver {

	public static interface ManageBuddiesContinuation {
		void withAddedOrRemovedBuddies(int count);
	};

	private enum Mode {
		ADD, REMOVE
	}

	public static void addBuddies(final List<User> users, final Manager manager, final ManageBuddiesContinuation continuation) {
		new ManageBuddiesTask(Mode.ADD, users, manager, continuation);
	}

	public static void addBuddy(final User user, final Manager manager, final ManageBuddiesContinuation continuation) {
		new ManageBuddiesTask(Mode.ADD, Collections.singletonList(user), manager, continuation);
	}

	public static void removeBuddy(final User user, final Manager manager, final ManageBuddiesContinuation continuation) {
		new ManageBuddiesTask(Mode.REMOVE, Collections.singletonList(user), manager, continuation);
	}

	private final ManageBuddiesContinuation	_continuation;
	private final UserController			_controller;
	private int								_count;
	private final Manager					_manager;
	private final Mode						_mode;
	private final List<User>				_users	= new ArrayList<User>();

	private ManageBuddiesTask(final Mode mode, final List<User> users, final Manager manager, final ManageBuddiesContinuation continuation) {
		_mode = mode;
		_users.addAll(users);
		_manager = manager;
		_continuation = continuation;
		_controller = new UserController(this);

		processNextOrFinish();
	}

	private User popUser() {
		if (_users == null) {
			return null;
		}
		if (_users.isEmpty()) {
			return null;
		}
		return _users.remove(0);
	}

	private void processNextOrFinish() {
		final User sessionUser = Session.getCurrentSession().getUser();
		final List<User> sessionUserBuddies = sessionUser.getBuddyUsers();
		User user;
		do {
			user = popUser();
		} while ((user != null)
				&& (sessionUser.equals(user) || ((sessionUserBuddies != null) && (((_mode == Mode.ADD) && sessionUserBuddies.contains(user)) || ((_mode == Mode.REMOVE) && !sessionUserBuddies
						.contains(user))))));

		if (user != null) {
			_controller.setUser(user);
			switch (_mode) {
			case ADD:
				_controller.addAsBuddy();
				break;

			case REMOVE:
				_controller.removeAsBuddy();
				break;
			}
			return;
		}

		for (final ValueStore valueStore : _manager.getSessionUserValueStores()) {
			
			// besides setting all value stores dirty, we also modify the value
			// immediately so that we minimize the time while the value is stale
			final Integer oldNumber = valueStore.getValue(Constant.NUMBER_BUDDIES);
			if (oldNumber != null) {
				final int newNumber = _mode == Mode.ADD ? oldNumber + _count : oldNumber - _count;
				valueStore.putValue(Constant.NUMBER_BUDDIES, newNumber);
			}
			
			valueStore.setAllDirty();
		}
		if (_continuation != null) {
			_continuation.withAddedOrRemovedBuddies(_count);
		}
	}

	public void requestControllerDidFail(final RequestController aRequestController, final Exception anException) {
		// just work on remainder users
		processNextOrFinish();
	}

	public void requestControllerDidReceiveResponse(final RequestController aRequestController) {
		++_count;
		processNextOrFinish();
	}

	public void userControllerDidFailOnEmailAlreadyTaken(final UserController controller) {
	}

	public void userControllerDidFailOnInvalidEmailFormat(final UserController controller) {
	}

	public void userControllerDidFailOnUsernameAlreadyTaken(final UserController controller) {
	}
}
