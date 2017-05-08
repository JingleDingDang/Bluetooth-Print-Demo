package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;
import com.example.linqu.bledemo.utils.DataUtils;

import java.lang.ref.WeakReference;


public class CheckKeyActivity extends Activity implements OnClickListener {

	private EditText editTextInputKey;

	private static Handler mHandler = null;
	private static String TAG = "CheckKeyActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkkey);

		findViewById(R.id.buttonCheckKey).setOnClickListener(this);
		editTextInputKey = (EditText) findViewById(R.id.editTextInputKey);

		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WorkService.delHandler(mHandler);
		mHandler = null;
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonCheckKey: {
			String strKey = editTextInputKey.getText().toString();
			if (strKey == null)
				return;
			if (strKey.length() == 0)
				return;

			byte[] key = strKey.getBytes();
			if (key.length != 8) {
				break;
			}
			byte[] random = DataUtils.getRandomByteArray(8);
			// 不要直接和Pos打交道，要通过workThread来交流
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, key);
				data.putByteArray(Global.BYTESPARA2, random);
				WorkService.workThread.handleCmd(Global.CMD_POS_CHECKKEY,
						data);
			} else {
				Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<CheckKeyActivity> mActivity;

		MHandler(CheckKeyActivity activity) {
			mActivity = new WeakReference<CheckKeyActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CheckKeyActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_CHECKKEYRESULT: {
				int result = msg.arg1;
				Toast.makeText(theActivity, (result == 1) ? Global.toast_success : Global.toast_fail,
						Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			}
		}
	}

}
