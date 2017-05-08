package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
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


public class ConnectBTMacActivity extends Activity implements OnClickListener {

	private ProgressDialog dialog;
	private EditText editText;

	private static Handler mHandler = null;
	private static String TAG = "ConnectBTMacActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectbtmac);

		editText = (EditText) findViewById(R.id.editTextInputMacAddress);
		findViewById(R.id.buttonConnectMacAddress).setOnClickListener(this);
		dialog = new ProgressDialog(this);

		SharedPreferences settings = getSharedPreferences(
				Global.PREFERENCES_FILENAME, 0);
		editText.setText(settings.getString(Global.PREFERENCES_BTADDRESS, ""));

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
		case R.id.buttonConnectMacAddress: {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (null == adapter) {
				finish();
				break;
			}

			if (!adapter.isEnabled()) {
				if (adapter.enable()) {
					while (!adapter.isEnabled())
						;
					Log.v(TAG, "Enable BluetoothAdapter");
				} else {
					finish();
					break;
				}
			}

			String address = editText.getText().toString().trim();
			if (!BluetoothAdapter.checkBluetoothAddress(address)) {
				Toast.makeText(this, "Invalid address, eg:01:23:45:67:89:AB",
						Toast.LENGTH_LONG).show();
				break;
			}

			SharedPreferences settings = getSharedPreferences(
					Global.PREFERENCES_FILENAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(Global.PREFERENCES_BTADDRESS, address);
			editor.commit();
			dialog.setMessage(Global.toast_connecting + " " + address);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
			WorkService.workThread.connectBt(address);

			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<ConnectBTMacActivity> mActivity;

		MHandler(ConnectBTMacActivity activity) {
			mActivity = new WeakReference<ConnectBTMacActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ConnectBTMacActivity theActivity = mActivity.get();
			switch (msg.what) {
			/**
			 * DrawerService 的 onStartCommand会发送这个消息
			 */

			case Global.MSG_WORKTHREAD_SEND_CONNECTBTRESULT: {
				int result = msg.arg1;
				Toast.makeText(
						theActivity,
						(result == 1) ? Global.toast_success
								: Global.toast_fail, Toast.LENGTH_SHORT).show();
				Log.v(TAG, "Connect Result: " + result);
				theActivity.dialog.cancel();
				break;
			}

			}
		}
	}

}
