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

import java.lang.ref.WeakReference;


public class SetKeyActivity extends Activity implements OnClickListener {

	private EditText editTextInputKey;

	private static Handler mHandler = null;
	private static String TAG = "SetKeyActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setkey);

		findViewById(R.id.buttonSetKey).setOnClickListener(this);
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
		case R.id.buttonSetKey: {
			String strKey = editTextInputKey.getText().toString();
			if (strKey == null)
				break;
			if (strKey.length() == 0)
				break;

			byte[] key = strKey.getBytes();
			if (key.length != 8) {
				break;
			}

			// 不要直接和Pos打交道，要通过workThread来交流
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, key);
				WorkService.workThread.handleCmd(Global.CMD_POS_SETKEY, data);
			} else {
				Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<SetKeyActivity> mActivity;

		MHandler(SetKeyActivity activity) {
			mActivity = new WeakReference<SetKeyActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SetKeyActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_SETKEYRESULT: {
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
