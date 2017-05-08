package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.example.linqu.bledemo.utils.IPString;

import java.lang.ref.WeakReference;

public class ConnectIPActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "ConnectIPActivity";

	EditText inputIp, inputPort;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectip);

		findViewById(R.id.buttonConnectIP).setOnClickListener(this);
		inputIp = (EditText) findViewById(R.id.editTextInputIp);
		inputPort = (EditText) findViewById(R.id.editTextInputPort);
		dialog = new ProgressDialog(this);

		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);

		SharedPreferences settings = getSharedPreferences(
				Global.PREFERENCES_FILENAME, 0);
		inputIp.setText(settings.getString(Global.PREFERENCES_IPADDRESS, ""));
		inputPort.setText(""
				+ settings.getInt(Global.PREFERENCES_PORTNUMBER, 9100));
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

		case R.id.buttonConnectIP:
			boolean valid = false;
			int port = 9100;
			String ip = "";
			try {
				ip = inputIp.getText().toString();
				if (null == IPString.IsIPValid(ip))
					throw new Exception("Invalid IP Address");
				port = Integer.parseInt(inputPort.getText().toString());
				valid = true;
			} catch (NumberFormatException e) {
				Toast.makeText(this, "Invalid Port Number", Toast.LENGTH_LONG)
						.show();
				valid = false;
			} catch (Exception e) {
				Toast.makeText(this, "Invalid IP Address", Toast.LENGTH_LONG)
						.show();
				valid = false;
			}
			if (valid) {
				SharedPreferences settings = getSharedPreferences(
						Global.PREFERENCES_FILENAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(Global.PREFERENCES_IPADDRESS, ip);
				editor.putInt(Global.PREFERENCES_PORTNUMBER, port);
				editor.commit();

				// 保存好偏好设置。然后进行下一步连接操作。
				dialog.setMessage(Global.toast_connecting + " " + ip + ":"
						+ port);
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				dialog.show();
				WorkService.workThread.connectNet(ip, port);
			}
			break;

		}

	}

	static class MHandler extends Handler {

		WeakReference<ConnectIPActivity> mActivity;

		MHandler(ConnectIPActivity activity) {
			mActivity = new WeakReference<ConnectIPActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ConnectIPActivity theActivity = mActivity.get();
			switch (msg.what) {
			/**
			 * DrawerService 的 onStartCommand会发送这个消息
			 */

			case Global.MSG_WORKTHREAD_SEND_CONNECTNETRESULT: {
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