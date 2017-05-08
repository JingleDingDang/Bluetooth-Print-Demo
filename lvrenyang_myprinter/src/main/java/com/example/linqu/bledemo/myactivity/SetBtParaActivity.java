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


public class SetBtParaActivity extends Activity implements OnClickListener {

	private EditText editTextInputBtKey;
	private EditText editTextInputBtName;

	private static Handler mHandler = null;
	private static String TAG = "SetBtParaActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setbtpara);

		findViewById(R.id.buttonSetBtPara).setOnClickListener(this);
		editTextInputBtName = (EditText) findViewById(R.id.editTextInputBtName);
		editTextInputBtKey = (EditText) findViewById(R.id.editTextInputBtKey);

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
		case R.id.buttonSetBtPara: {
			String strName = editTextInputBtName.getText().toString();
			String strKey = editTextInputBtKey.getText().toString();

			if ((strName == null) || (strKey == null))
				return;
			if ((strName.length() == 0) || (strKey.length() == 0))
				return;

			// 不要直接和Pos打交道，要通过workThread来交流
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putString(Global.STRPARA1, strName);
				data.putString(Global.STRPARA2, strKey);
				WorkService.workThread.handleCmd(Global.CMD_PORTABLE_SETBTPARA,
						data);
			} else {
				Toast.makeText(this, Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<SetBtParaActivity> mActivity;

		MHandler(SetBtParaActivity activity) {
			mActivity = new WeakReference<SetBtParaActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SetBtParaActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_PORTABLE_SETBTPARA_RESULT: {
				int result = msg.arg1;
				Toast.makeText(
						theActivity,
						(result == 1) ? Global.toast_success
								: Global.toast_fail, Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			}
		}
	}

}
