package com.example.linqu.bledemo.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class JSAndroidActivity extends Activity {

	private WebView mWebView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showWebView();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void showWebView() { // webView与js交互代码
		try {
			mWebView = new WebView(this);
			setContentView(mWebView);

			mWebView.requestFocus();

			mWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					JSAndroidActivity.this.setTitle("Loading...");
					JSAndroidActivity.this.setProgress(progress);

					if (progress >= 80) {
						JSAndroidActivity.this.setTitle("JsAndroid Test");
					}
				}
			});

			mWebView.setOnKeyListener(new View.OnKeyListener() { // webview can
																	// go back
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& mWebView.canGoBack()) {
						mWebView.goBack();
						return true;
					}
					return false;
				}
			});

			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDefaultTextEncodingName("utf-8");

			mWebView.addJavascriptInterface(this, "jsObj");
			Locale locale = getResources().getConfiguration().locale;
			String language = locale.getLanguage();
			if (language.endsWith("zh"))
				mWebView.loadUrl("file:///android_asset/index.html");
			else
				mWebView.loadUrl("file:///android_asset/index_en.html");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@JavascriptInterface
	public String Print(final String param) {
		if (null == param)
			return "";

		byte[] buf = null;
		try {
			buf = param.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return e.toString();
		}

		if (WorkService.workThread.isConnected()) {
			Bundle data = new Bundle();
			data.putByteArray(Global.BYTESPARA1, buf);
			data.putInt(Global.INTPARA1, 0);
			data.putInt(Global.INTPARA2, buf.length);
			WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			return getString(R.string.printing);
		} else {
			return getString(R.string.printernotconnected);
		}
	}

	@JavascriptInterface
	public String HtmlcallJava() {
		return "Html call Java";
	}

	@JavascriptInterface
	public String HtmlcallJava2(final String param) {
		return "Html call Java : " + param;
	}

	@JavascriptInterface
	public void JavacallHtml() {
		runOnUiThread(new Runnable() {
			public void run() {
				mWebView.loadUrl("javascript: showFromHtml()");
				Toast.makeText(JSAndroidActivity.this, "clickBtn",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@JavascriptInterface
	public void JavacallHtml2() {
		runOnUiThread(new Runnable() {
			public void run() {
				mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
				Toast.makeText(JSAndroidActivity.this, "clickBtn2",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

}
