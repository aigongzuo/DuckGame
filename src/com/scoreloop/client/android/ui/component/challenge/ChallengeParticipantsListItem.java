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

class ChallengeParticipantsListItem extends BaseListItem {

	private final String	_contenderName;
	private String			_contenderStats;
	private final String	_contestantName;
	private String			_contestantStats;

	public ChallengeParticipantsListItem(final Context context, final String contenderName, final String contenderStats,
			final String contestantName, final String contestantStats) {
		super(context, null, null);
		_contenderName = contenderName;
		_contenderStats = contenderStats;
		_contestantName = contestantName;
		_contestantStats = contestantStats;
	}

	@Override
	public int getType() {
		return Constant.LIST_ITEM_TYPE_CHALLENGE_PARTICIPANTS;
	}

	@Override
	public View getView(View view, final ViewGroup parent) {
		if (view == null) {
			view = getLayoutInflater().inflate(R.layout.sl_list_item_challenge_participants, null);
		}
		prepareView(view);
		return view;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	protected void prepareView(final View view) {
		final TextView contenderNameView = (TextView) view.findViewById(R.id.contender_name);
		contenderNameView.setText(_contenderName);

		final TextView contenderStatsView = (TextView) view.findViewById(R.id.contender_stats);
		contenderStatsView.setText(_contenderStats);

		final TextView contestantNameView = (TextView) view.findViewById(R.id.contestant_name);
		contestantNameView.setText(_contestantName);

		final TextView contestantStatsView = (TextView) view.findViewById(R.id.contestant_stats);
		contestantStatsView.setText(_contestantStats);
	}

	public void setContenderStats(final String contenderStats) {
		_contenderStats = contenderStats;
	}

	public void setContestantStats(final String contestantStats) {
		_contestantStats = contestantStats;
	}
}
