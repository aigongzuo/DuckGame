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

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.model.Challenge;
import com.scoreloop.client.android.ui.component.base.ComponentActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.util.ImageDownloader;

public class ChallengeOpenListItem extends BaseListItem {

	static class ViewHolder {
		ImageView	icon;
		TextView	subTitle;
		TextView	subTitle2;
		TextView	title;
	}

	private final Challenge	_challenge;

	public ChallengeOpenListItem(final ComponentActivity context, final Drawable drawable, final Challenge challenge) {
		super(context, drawable, null);
		_challenge = challenge;
	}

	Challenge getChallenge() {
		return _challenge;
	}

	private ComponentActivity getComponentActivity() {
		return (ComponentActivity) getContext();
	}

	@Override
	public int getType() {
		return Constant.LIST_ITEM_TYPE_CHALLENGE_OPEN;
	}

	@Override
	public View getView(View view, final ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			view = getLayoutInflater().inflate(R.layout.sl_list_item_challenge_open, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.sl_icon);
			holder.title = (TextView) view.findViewById(R.id.sl_title);
			holder.subTitle = (TextView) view.findViewById(R.id.sl_subtitle);
			holder.subTitle2 = (TextView) view.findViewById(R.id.sl_subtitle2);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.icon.setImageDrawable(getDrawable());
		ImageDownloader.downloadImage(_challenge.getContender().getImageUrl(),
				getContext().getResources().getDrawable(R.drawable.sl_icon_user), holder.icon);
		holder.title.setText(_challenge.getContender().getDisplayName());
		if (((ComponentActivity) getContext()).getGame().hasModes()) {
			holder.subTitle.setText(((ComponentActivity) getContext()).getModeString(_challenge.getMode()));
		}
		holder.subTitle2.setText(StringFormatter.formatMoney(_challenge.getStake(), getComponentActivity().getConfiguration()));

		return view;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
