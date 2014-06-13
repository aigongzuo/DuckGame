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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phundroid.duck.R;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.framework.BaseListItem;

public class ChallengeSimpleListItem extends BaseListItem {

	static class ViewHolder {
		TextView title;
	}

	private final String _label;

	public ChallengeSimpleListItem(final Context context, final String label) {
		super(context, null, null);
		_label = label;
	}

	@Override
	public int getType() {
		return Constant.LIST_ITEM_TYPE_CHALLENGE_SIMPLE;
	}

	@Override
	public View getView(View view, final ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			view = getLayoutInflater().inflate(R.layout.sl_list_item_challenge_simple, null);
			holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.sl_title);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.title.setText(_label);

		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}
}
