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

package com.scoreloop.client.android.ui.framework;

import java.util.List;

import com.phundroid.duck.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ShortcutView extends SegmentedView {

	private List<ShortcutDescription>	_shortcutDescriptions	= null;

	public ShortcutView(final Context context, final AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public void setDescriptions(final Activity activity, final List<ShortcutDescription> shortcutDescriptions) {

		// performance improvement in case descriptions have not changed
		if ((_shortcutDescriptions != null) && _shortcutDescriptions.equals(shortcutDescriptions)) {
			_shortcutDescriptions = shortcutDescriptions;
			return;
		}

		removeAllViews();

		_shortcutDescriptions = shortcutDescriptions;

		for (final ShortcutDescription shortcutDescription : shortcutDescriptions) {
			final ViewGroup viewGroup = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.sl_tab_shortcut, null);
			final LayoutParams lp = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			lp.gravity = Gravity.CENTER;
			viewGroup.setLayoutParams(lp);
			viewGroup.setId(shortcutDescription.getTextId());
			((ImageView) viewGroup.findViewById(R.id.sl_image_tab_view)).setImageResource(shortcutDescription.getImageId());
			addView(viewGroup);
		}

		prepareUsage();
	}

	@Override
	protected void setSegmentEnabled(final int segment, final boolean enabled) {
		final View view = getChildAt(segment);
		final ShortcutDescription shortcutDescription = _shortcutDescriptions.get(segment);

		if (enabled) {
			((ImageView) view.findViewById(R.id.sl_image_tab_view)).setImageResource(shortcutDescription.getActiveImageId());
			view.setBackgroundResource(R.drawable.sl_shortcut_highlight);
		} else {
			((ImageView) view.findViewById(R.id.sl_image_tab_view)).setImageResource(shortcutDescription.getImageId());
			view.setBackgroundDrawable(null);
		}
	}
}
