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

package com.scoreloop.client.android.ui;

import com.scoreloop.client.android.core.model.Entity;
import com.scoreloop.client.android.core.model.Score;
import com.scoreloop.client.android.ui.component.base.StringFormatter;
import com.scoreloop.client.android.ui.component.post.PostOverlayActivity;

/**
 * Use this activity after a gameplay and after you have submitted a score to Scoreloop in order to allow the game player to
 * post a message about the score to a social network.
 * 
 * Ensure that you have properly initialized the ScoreloopManagerSingleton,
 * before starting this activity using an <a href="http://developer.android.com/reference/android/content/Intent.html">android.content.Intent</a>.
 */
public class PostScoreOverlayActivity extends PostOverlayActivity {

	@Override
	protected Entity getMessageTarget() {
		final StandardScoreloopManager manager = StandardScoreloopManager.getFactory(ScoreloopManagerSingleton.get());
		Entity target = manager.getLastChallenge();
		if (target == null || target.getIdentifier() == null) {
			target = manager.getLastScore();
		}
		return target;
	}

	@Override
	protected String getPostText() {
		final StandardScoreloopManager manager = StandardScoreloopManager.getFactory(ScoreloopManagerSingleton.get());
		final Entity target = (Entity) getMessageTarget();
		if (target instanceof Score) {
			return "Score: " + StringFormatter.formatScore((Score)target, manager.getConfiguration());
		} else {
			return "Challenge";
		}
	}
}
