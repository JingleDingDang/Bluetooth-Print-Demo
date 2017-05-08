package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;
import com.example.linqu.bledemo.utils.DataUtils;
import com.example.linqu.bledemo.utils.TimeUtils;

import java.lang.ref.WeakReference;


public class SearchBTActivity extends Activity implements OnClickListener {

	private LinearLayout linearlayoutdevices;
	private ProgressBar progressBarSearchStatus;
	private ProgressDialog dialog;

	private BroadcastReceiver broadcastReceiver = null;
	private IntentFilter intentFilter = null;

	private static Handler mHandler = null;
	private static String TAG = "SearchBTActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchbt);

		findViewById(R.id.buttonSearch).setOnClickListener(this);
		progressBarSearchStatus = (ProgressBar) findViewById(R.id.progressBarSearchStatus);
		linearlayoutdevices = (LinearLayout) findViewById(R.id.linearlayoutdevices);
		dialog = new ProgressDialog(this);

		initBroadcast();

		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WorkService.delHandler(mHandler);
		mHandler = null;
		uninitBroadcast();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonSearch: {
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
			
			if(null != WorkService.workThread)
			{
				WorkService.workThread.disconnectBt();
				TimeUtils.WaitMs(10);
			}
			adapter.cancelDiscovery();
			linearlayoutdevices.removeAllViews();
			TimeUtils.WaitMs(10);
			adapter.startDiscovery();
			break;
		}
		}
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					if (device == null)
						return;
					final String address = device.getAddress();
					String name = device.getName();
					if (name == null)
						name = "BT";
					else if (name.equals(address))
						name = "BT";
					Button button = new Button(context);
					button.setText(name + ": " + address);
					button.setGravity(Gravity.CENTER_VERTICAL
							| Gravity.LEFT);
					button.setOnClickListener(new OnClickListener() {

						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							WorkService.workThread.disconnectBt();
							// 只有没有连接且没有在用，这个才能改变状态
							dialog.setMessage(Global.toast_connecting + " "
									+ address);
							dialog.setIndeterminate(true);
							dialog.setCancelable(false);
							dialog.show();
							WorkService.workThread.connectBt(address);
						}
					});
					button.getBackground().setAlpha(100);
					linearlayoutdevices.addView(button);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {
					progressBarSearchStatus.setIndeterminate(true);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {
					progressBarSearchStatus.setIndeterminate(false);
				}

			}

		};
		intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	static class MHandler extends Handler {

		WeakReference<SearchBTActivity> mActivity;

		MHandler(SearchBTActivity activity) {
			mActivity = new WeakReference<SearchBTActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SearchBTActivity theActivity = mActivity.get();
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
				if (1 == result) {
					PrintTest();
				}
				break;
			}

			}
		}

		void PrintTest() {
			String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n0123456789\n";
			byte[] tmp1 = { 0x1b, 0x40, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA,
					(byte) 0xD4, (byte) 0xD2, (byte) 0xB3, 0x0A };
			byte[] tmp2 = { 0x1b, 0x21, 0x01 };
			byte[] tmp3 = { 0x0A, 0x0A, 0x0A, 0x0A };
			byte[] buf = DataUtils.byteArraysToBytes(new byte[][] { tmp1,
					str.getBytes(), tmp2, str.getBytes(), tmp3 });
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				WorkService.workThread.handleCmd(Global.CMD_WRITE, data);
			} else {
				Toast.makeText(mActivity.get(), Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
