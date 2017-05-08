package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;

import java.lang.ref.WeakReference;


public class CutterActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "CutterActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cutter);

		findViewById(R.id.buttonFullCut).setOnClickListener(this);
		findViewById(R.id.buttonHalfCut).setOnClickListener(this);
		findViewById(R.id.buttonFeedAndCut).setOnClickListener(this);

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
		case R.id.buttonFullCut:{
			// 全切命令有2个
			byte[] buf = new byte[]{0x1b, 0x69};
			//byte[] buf = new byte[]{0x1d, 0x56, 0x00};
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			} else {
				Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		case R.id.buttonHalfCut:{
			// 半切命令有2个
			byte[] buf = new byte[]{0x1b, 0x6d};
			//byte[] buf = new byte[]{0x1d, 0x56, 0x01};
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			} else {
				Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		case R.id.buttonFeedAndCut:{
			// 走纸到切刀位置并切纸
			byte[] buf = new byte[]{0x1d, 0x56, 0x42, 0x00};
			
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			} else {
				Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		}
	}

	static class MHandler extends Handler {

		WeakReference<CutterActivity> mActivity;

		MHandler(CutterActivity activity) {
			mActivity = new WeakReference<CutterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CutterActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_WRITERESULT: {
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
