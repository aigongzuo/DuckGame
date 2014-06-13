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

package com.scoreloop.client.android.ui.component.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.UserController;
import com.scoreloop.client.android.core.controller.UserControllerObserver;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.PackageManager;
import com.scoreloop.client.android.ui.component.base.StandardListItem;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.ValueStore;

public class ProfileSettingsListActivity extends ComponentListActivity<BaseListItem> implements UserControllerObserver {

	/**
	 * \internal
	 * consider separation
	 */
	class EditDialog {
		private final EditText	_edit;
		private final View		_layout;

		public EditDialog(final View layout, final EditText edit) {
			_layout = layout;
			_edit = edit;
		}

		public EditText getEdit() {
			return _edit;
		}

		public View getLayout() {
			return _layout;
		}
	}

	class UserProfileListAdapter extends BaseListAdapter<BaseListItem> {

		public UserProfileListAdapter(final Context context) {
			super(context);
			// screen contains static main list items
			add(new CaptionListItem(context, null, getString(R.string.sl_manage_account)));
			add(_changePictureItem);
			add(_changeUsernameItem);
			if (getSessionUser().getEmailAddress() != null) {
				add(_changeEmailItem);
			}
		}
	}

	private ProfileListItem	_changeEmailItem;
	private ProfileListItem	_changePictureItem;
	private ProfileListItem	_changeUsernameItem;
	private UserController	_userController;
	private String			_restoreEmail;

	private User getUpdateUser() {
		_restoreEmail = getSessionUser().getEmailAddress();
		return getSessionUser();
	}

	private void restoreUpdateUser() {
		getSessionUser().setEmailAddress(_restoreEmail);
	}

	private void changeEmailDialog() {
		final EditDialog editDialog = getEditDialog(getString(R.string.sl_change_email), getString(R.string.sl_current),
				getSessionUser().getEmailAddress(), getString(R.string.sl_new), null);
		final Dialog dialog = new Dialog(this, R.style.sl_dialog);
		dialog.setContentView(editDialog.getLayout());
		final TextView hint = (TextView) editDialog.getLayout().findViewById(R.id.sl_dialog_hint);
		Button ok = (Button)editDialog.getLayout().findViewById(R.id.sl_button_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String newEmail = editDialog.getEdit().getText().toString().trim();
				if(!isValidEmailFormat(newEmail)) {
					hint.setText(getString(R.string.sl_please_email_address));
				}
				else {
					dialog.dismiss();
					User user = getUpdateUser();
					user.setEmailAddress(newEmail);
					updateUser(user);
				}
			}
		});
		Button cancel = (Button)editDialog.getLayout().findViewById(R.id.sl_button_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void changeUsernameDialog() {
		final EditDialog editDialog = getEditDialog(getString(R.string.sl_change_username), getString(R.string.sl_current),
					getSessionUser().getLogin(), getString(R.string.sl_new), null);
		final Dialog dialog = new Dialog(this, R.style.sl_dialog);
		dialog.setContentView(editDialog.getLayout());
		Button ok = (Button)editDialog.getLayout().findViewById(R.id.sl_button_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				String newUsername = editDialog.getEdit().getText().toString().trim();
				User user = getUpdateUser();
				user.setLogin(newUsername);
				updateUser(user);
			}
		});
		Button cancel = (Button)editDialog.getLayout().findViewById(R.id.sl_button_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private EditDialog getEditDialog(final String title,
									final String currentLabel,
									final String currentText,
									final String newLabel,
									final String newText)
	{
		final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.sl_dialog_profile_edit, (ViewGroup) findViewById(R.id.sl_user_profile_edit_layout));
		final TextView tvTitle = (TextView) layout.findViewById(R.id.sl_title);
		tvTitle.setText(title);
		final TextView tvCurrentLabel = (TextView) layout.findViewById(R.id.sl_user_profile_edit_current_label);
		tvCurrentLabel.setText(currentLabel);
		final TextView tvCurrentText = (TextView) layout.findViewById(R.id.sl_user_profile_edit_current_text);
		tvCurrentText.setText(currentText);
		final TextView tvNewLabel = (TextView) layout.findViewById(R.id.sl_user_profile_edit_new_label);
		tvNewLabel.setText(newLabel);
		final EditText tvNewText = (EditText) layout.findViewById(R.id.sl_user_profile_edit_new_text);
		tvNewText.setText(newText);
		return new EditDialog(layout, tvNewText);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		User user = getSessionUser();
		_userController = new UserController(this);
		_changePictureItem = new ProfileListItem(this, getResources().getDrawable(R.drawable.sl_icon_change_picture),
				getString(R.string.sl_change_picture), getString(R.string.sl_change_picture_details));
		_changeUsernameItem = new ProfileListItem(this, getResources().getDrawable(R.drawable.sl_icon_change_username),
				getString(R.string.sl_change_username), user.getLogin());
		_changeEmailItem = new ProfileListItem(this, getResources().getDrawable(R.drawable.sl_icon_change_email),
				getString(R.string.sl_change_email), user.getEmailAddress());
		if (user.getLogin() == null || user.getEmailAddress() == null) {
			showSpinnerFor(_userController);
			_userController.loadUser();
		} else {
			setListAdapter(new UserProfileListAdapter(this));
		}
		if (!PackageManager.isScoreloopAppInstalled(this)) {
			showFooter(new StandardListItem<Void>(this, getResources().getDrawable(R.drawable.sl_icon_scoreloop),
					getString(R.string.sl_slapp_title), getString(R.string.sl_slapp_subtitle), null));
		}
	}

	@Override
	protected void onFooterItemClick(final BaseListItem footerItem) {
		hideFooter();
		PackageManager.installScoreloopApp(this);
	}

	@Override
	public void onListItemClick(final BaseListItem item) {
		if (item == _changeUsernameItem) {
			if (getSessionUser().getEmailAddress() == null) {
				showFirstTimeDialog();
			} else {
				changeUsernameDialog();
			}
		} else if (item == _changePictureItem) {
			display(getFactory().createProfileSettingsPictureScreenDescription(getSessionUser()));
		} else if (item == _changeEmailItem) {
			changeEmailDialog();
		}
	}

	@Override
	public void onRefresh(final int flags) {
		_changeEmailItem.setSubTitle(getSessionUser().getEmailAddress());
		_changeUsernameItem.setSubTitle(getSessionUser().getLogin());
		getBaseListAdapter().notifyDataSetChanged();
	}

	@Override
	protected void requestControllerDidFailSafe(RequestController requestController, Exception exception) {
		super.requestControllerDidFailSafe(requestController, exception);
		restoreUpdateUser();
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController controller) {
		final ValueStore store = getContentValues();
		store.putValue(Constant.USER_NAME, getSessionUser().getDisplayName());
		store.putValue(Constant.USER_IMAGE_URL, getSessionUser().getImageUrl());
		setListAdapter(new UserProfileListAdapter(this));
		hideSpinnerFor(controller);
		setNeedsRefresh();
	}

	private boolean isValidEmailFormat(String email) {
		Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	private void showFirstTimeDialog() {
		final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.sl_dialog_profile_edit_initial,
				(ViewGroup) findViewById(R.id.sl_dialog_profile_edit_initial_layout));
		TextView tvCurrentUsername = (TextView) layout.findViewById(R.id.sl_user_profile_edit_initial_current_text);
		tvCurrentUsername.setText(getSessionUser().getLogin());
		final EditText username = (EditText) layout.findViewById(R.id.sl_user_profile_edit_initial_username_text);
		final EditText email = (EditText) layout.findViewById(R.id.sl_user_profile_edit_initial_email_text);
		final TextView hint = (TextView) layout.findViewById(R.id.sl_dialog_hint);
		final Dialog dialog = new Dialog(this, R.style.sl_dialog);
		dialog.setContentView(layout);
		Button ok = (Button) layout.findViewById(R.id.sl_button_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String newEmail = email.getText().toString().trim();
				String newUsername = username.getText().toString().trim();
				if(!isValidEmailFormat(newEmail)) {
					hint.setText(getString(R.string.sl_please_email_address));
				}
				else {
					dialog.dismiss();
					User user = getUpdateUser();
					user.setLogin(newUsername);
					user.setEmailAddress(newEmail);
					updateUser(user);
				}
			}
		});
		Button cancel = (Button) layout.findViewById(R.id.sl_button_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showErrorDialog(String title, String text) {
		final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.sl_dialog_error,
				(ViewGroup) findViewById(R.id.sl_dialog_error_layout));
		TextView tvTitle = (TextView) layout.findViewById(R.id.sl_title);
		tvTitle.setText(title);
		TextView tvErrorMessage = (TextView) layout.findViewById(R.id.sl_error_message);
		tvErrorMessage.setText(text);
		final Dialog dialog = new Dialog(this, R.style.sl_dialog);
		dialog.setContentView(layout);
		Button ok = (Button) layout.findViewById(R.id.sl_button_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void updateUser(final User user) {
		getHandler().post(new Runnable() {
			public void run() {
				showSpinnerFor(_userController);
				_userController.setUser(user);
				_userController.submitUser();
			}
		});
	}

	public void userControllerDidFailOnEmailAlreadyTaken(final UserController controller) {
		hideSpinnerFor(controller);
		showErrorDialog(getString(R.string.sl_error_title_email_already_taken),
				getString(R.string.sl_error_message_email_already_taken));
		restoreUpdateUser();
	}

	public void userControllerDidFailOnInvalidEmailFormat(final UserController controller) {
		hideSpinnerFor(controller);
		showErrorDialog(getString(R.string.sl_error_title_invalid_email_format),
				getString(R.string.sl_error_message_invalid_email));
		restoreUpdateUser();
	}

	public void userControllerDidFailOnUsernameAlreadyTaken(final UserController controller) {
		hideSpinnerFor(controller);
		showErrorDialog(getString(R.string.sl_error_title_username_already_taken),
				getString(R.string.sl_error_message_username_already_taken));
		restoreUpdateUser();
	}
}
