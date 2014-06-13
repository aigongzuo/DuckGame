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

package com.scoreloop.client.android.ui.component.score;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.phundroid.duck.R;
import com.scoreloop.client.android.core.controller.RankingController;
import com.scoreloop.client.android.core.controller.RequestController;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.model.Ranking;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.core.model.SearchList;
import com.scoreloop.client.android.core.model.User;
import com.scoreloop.client.android.ui.component.base.CaptionListItem;
import com.scoreloop.client.android.ui.component.base.ComponentListActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.component.base.Factory;
import com.scoreloop.client.android.ui.framework.BaseListAdapter;
import com.scoreloop.client.android.ui.framework.BaseListItem;
import com.scoreloop.client.android.ui.framework.PagingDirection;
import com.scoreloop.client.android.ui.framework.PagingListAdapter;
import com.scoreloop.client.android.ui.framework.ValueStore;
import com.scoreloop.client.android.ui.framework.ValueStore.Observer;

public class ScoreListActivity extends ComponentListActivity<ScoreListItem> implements Observer,
		PagingListAdapter.OnListItemClickListener<ScoreListItem> {

	private static final int	RANGE_LENGTH				= 20;

	private int					_cachedVerticalCenterOffset	= -1;
	private int					_highlightedPosition;
	private ScoresController	_highlightScoresController;
	private PagingDirection		_pagingDirection;
	private Ranking				_ranking;
	private RankingController	_rankingController;
	private ScoresController	_scoresController;

	@SuppressWarnings("unchecked")
	private PagingListAdapter<ScoreListItem> getPagingListAdapter() {
		final BaseListAdapter<?> baseAdapter = getBaseListAdapter();
		return (PagingListAdapter<ScoreListItem>) baseAdapter;
	}

	private int getVerticalCenterOffset() {
		if (_cachedVerticalCenterOffset == -1) {
			final ScoreListItem item = getBaseListAdapter().getItem(_highlightedPosition);
			final View itemView = item.getView(null, null);
			itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			_cachedVerticalCenterOffset = (getListView().getHeight() - itemView.getMeasuredHeight()) / 2;
		}
		return _cachedVerticalCenterOffset;
	}

	private boolean isHighlightedScore(final Score score) {
		return score.getUser().ownsSession(getSession());
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new PagingListAdapter<ScoreListItem>(this));

		final SearchList searchList = getActivityArguments().getValue(Constant.SEARCH_LIST, SearchList.getDefaultScoreSearchList());
		
		addObservedKeys(Constant.MODE);
		if (searchList.equals(SearchList.getBuddiesScoreSearchList())) {
			addObservedKeys(Constant.NUMBER_BUDDIES);
		}
		
		_scoresController = new ScoresController(getRequestControllerObserver());
		_scoresController.setSearchList(searchList);
		_scoresController.setRangeLength(RANGE_LENGTH);

		_rankingController = new RankingController(getRequestControllerObserver());
		_rankingController.setSearchList(searchList);

		_highlightScoresController = new ScoresController(getRequestControllerObserver());
		_highlightScoresController.setRangeLength(1);
		_highlightScoresController.setSearchList(searchList);
		
		setNeedsRefresh(PagingDirection.PAGE_TO_TOP);
	}

	@Override
	protected void onFooterItemClick(final BaseListItem footerItem) {
		if (footerItem.getType() == Constant.LIST_ITEM_TYPE_SCORE_HIGHLIGHTED) {
			display(getFactory().createProfileSettingsScreenDescription(null));
		}
	}

	private void onHighlightScores() {
		final List<Score> scores = _highlightScoresController.getScores();
		if (scores.size() > 0) {
			showFooter(new ScoreHighlightedListItem(this, scores.get(0), _ranking));
		}
	}

	@Override
	public void onListItemClick(final ScoreListItem item) {
		final Factory factory = getFactory();
		final User user = item.getTarget().getUser();

		if (user.ownsSession(getSession())) {
			display(factory.createProfileSettingsScreenDescription(user));
		} else {
			display(factory.createUserDetailScreenDescription(user, true));
		}
	}

	public void onPagingListItemClick(final PagingDirection pagingDirection) {
		setNeedsRefresh(pagingDirection);
	}

	private void onRanking() {
		_ranking = _rankingController.getRanking();
		final Integer rank = _ranking.getRank();

		if (rank != null) {

			// if we have a highlighted position, update its ranking and refresh the list
			if (_highlightedPosition != -1) {
				final PagingListAdapter<ScoreListItem> adapter = getPagingListAdapter();
				final ScoreHighlightedListItem highlightedItem = (ScoreHighlightedListItem) adapter.getContentItem(_highlightedPosition);
				highlightedItem.setRanking(_ranking);
				adapter.notifyDataSetChanged();
			}

			// otherwise load the score for the rank
			else {
				_highlightScoresController.setMode(getScreenValues().<Integer> getValue(Constant.MODE));
				_highlightScoresController.loadRangeAtRank(rank);
			}
		} else {
			// if no rank found, inform user via
			showFooter(new ScoreExcludedListItem(this));
		}
	}

	@Override
	public void onRefresh(final int flags) {
		showSpinnerFor(_scoresController);
		_scoresController.setMode(getScreenValues().<Integer> getValue(Constant.MODE));

		switch (_pagingDirection) {
		case PAGE_TO_TOP:
			_scoresController.loadRangeAtRank(1);
			break;

		case PAGE_TO_PREV:
			_scoresController.loadPreviousRange();
			break;

		case PAGE_TO_NEXT:
			_scoresController.loadNextRange();
			break;
		}
	}

	private void onScores() {
		final PagingListAdapter<ScoreListItem> adapter = getPagingListAdapter();
		adapter.clear();

		// fill adapter with scores
		final List<Score> scores = _scoresController.getScores();
		final int scoreCount = scores.size();
		for (int i = 0; i < scoreCount; ++i) {
			final Score score = scores.get(i);
			if (isHighlightedScore(score)) {
				_highlightedPosition = i;
				adapter.add(new ScoreHighlightedListItem(this, score, null));
			} else {
				adapter.add(new ScoreListItem(this, score));
			}
		}
		if (scoreCount == 0) {
			adapter.add(new CaptionListItem(this, null, getString(R.string.sl_no_scores)));
		}

		// fill adapter with paging navigators
		adapter
				.addPagingItems(_scoresController.hasPreviousRange(), _scoresController.hasPreviousRange(), _scoresController
						.hasNextRange());

		// if _highlightedPosition is valid, scroll to that position, otherwise to top or bottom (depending on paging direction)
		final ListView listView = getListView();
		if (_highlightedPosition != -1) {
			listView.setSelectionFromTop(_highlightedPosition, getVerticalCenterOffset());
		} else {
			if (_pagingDirection == PagingDirection.PAGE_TO_TOP) {
				listView.setSelection(0);
			} else if (_pagingDirection == PagingDirection.PAGE_TO_NEXT) {
				listView.setSelection(adapter.getFirstContentPosition());
			} else {
				listView.setSelection(adapter.getLastContentPosition());
			}
		}

		// load the rank for the user
		final Integer mode = getScreenValues().getValue(Constant.MODE);
		_rankingController.loadRankingForUserInGameMode(getUser(), mode);
	}

	@Override
	public void onValueChanged(final ValueStore valueStore, final String key, final Object oldValue, final Object newValue) {
		if (isValueChangedFor(key, Constant.MODE, oldValue, newValue)) {
			setNeedsRefresh(PagingDirection.PAGE_TO_TOP);
		} else if (isValueChangedFor(key, Constant.NUMBER_BUDDIES, oldValue, newValue)) {
			setNeedsRefresh(PagingDirection.PAGE_TO_TOP);
		}
	}

	@Override
	public void requestControllerDidReceiveResponseSafe(final RequestController aRequestController) {
		if (aRequestController == _scoresController) {
			onScores();
		} else if (aRequestController == _rankingController) {
			onRanking();
		} else if (aRequestController == _highlightScoresController) {
			onHighlightScores();
		}
	}

	private void setNeedsRefresh(final PagingDirection pagingDirection) {
		_highlightedPosition = -1;
		_pagingDirection = pagingDirection;
		hideFooter();
		setNeedsRefresh();
	}
}
