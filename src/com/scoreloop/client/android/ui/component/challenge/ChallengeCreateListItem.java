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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.util.ImageDownloader;

public class ChallengeCreateListItem extends BaseListItem {

	static class ViewHolder {
		ImageView icon;
		TextView title;
	}

	private final User _user;

	public ChallengeCreateListItem(final Context context, final Drawable drawable, final User user) {
		super(context, drawable, null);
		_user = user;
	}

	@Override
	public int getType() {
		return Constant.LIST_ITEM_TYPE_CHALLENGE_NEW;
	}

	@Override
	public View getView(View view, final ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			view = getLayoutInflater().inflate(R.layout.sl_list_item_icon_title, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.sl_icon);
			holder.title = (TextView) view.findViewById(R.id.sl_title);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Drawable drawable = getDrawable();
		holder.icon.setImageDrawable(drawable);
		if (_user == null) {
			holder.title.setText(getContext().getResources().getString(R.string.sl_against_anyone));
		} else {
			if(drawable == null) {
				drawable = getContext().getResources().getDrawable(R.drawable.sl_icon_user);
			}
			ImageDownloader.downloadImage(_user.getImageUrl(), drawable, holder.icon);
			holder.title.setText(_user.getDisplayName());
		}

		return view;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	User getUser() {
		return _user;
	}
}
