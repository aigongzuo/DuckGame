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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.phundroid.duck.R;
import com.scoreloop.client.android.ui.component.base.ComponentActivity;
import com.scoreloop.client.android.ui.component.base.Constant;
import com.scoreloop.client.android.ui.framework.BaseDialog;
import com.scoreloop.client.android.ui.framework.OkCancelDialog;
import com.scoreloop.client.android.ui.framework.BaseDialog.OnActionListener;

public class ChallengePaymentActivity extends ComponentActivity implements OnActionListener {

	private class PaymentWebViewClient extends WebViewClient {
		private boolean	_showsSpinner;

		@Override
		public void onPageFinished(final WebView view, final String url) {
			getContentValues().setDirty(Constant.USER_BALANCE);
			if (_showsSpinner) {
				hideSpinner();
				view.requestFocus();
				_showsSpinner = false;
			}
		}

		@Override
		public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
			if (!_showsSpinner) {
				showSpinner();
				_showsSpinner = true;
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			view.loadUrl(url);
			return true;
		}
	}

	private WebView	_webView;

	@Override
	protected boolean isBackAllowed() {
		final OkCancelDialog dialog = new OkCancelDialog(this);
		dialog.setText(getResources().getString(R.string.sl_leave_payment));
		dialog.setOnActionListener(this);
		showDialogSafe(dialog);
		return false;
	}

	public void onAction(final BaseDialog dialog, final int actionId) {
		if (actionId == OkCancelDialog.BUTTON_OK) {
			dialog.dismiss();
			displayPrevious();
		} else {
			dialog.dismiss();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sl_challenge_payment, true);

		_webView = (WebView) findViewById(R.id.sl_webview);
		_webView.setWebViewClient(new PaymentWebViewClient());

		final WebSettings settings = _webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setBuiltInZoomControls(true);

		_webView.loadUrl(getSession().getPaymentUrl());
		_webView.requestFocus();
	}
}
