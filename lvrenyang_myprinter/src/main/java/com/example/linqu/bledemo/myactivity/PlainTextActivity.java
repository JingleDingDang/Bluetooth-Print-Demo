package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;
import com.example.linqu.bledemo.utils.DataUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class PlainTextActivity extends Activity implements OnClickListener {

	private EditText editTextSend;
	private Button buttonSend, buttonClearSend;
	private RadioButton radioButtonUTF8, radioButtonGBK, radioButtonKOREA;

	private static Handler mHandler = null;
	private static String TAG = "PlainTextActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plaintext);

		editTextSend = (EditText) findViewById(R.id.editTextSend);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonClearSend = (Button) findViewById(R.id.buttonClearSend);
		radioButtonGBK = (RadioButton) findViewById(R.id.radioButtonGBK);
		radioButtonUTF8 = (RadioButton) findViewById(R.id.radioButtonUTF8);
		radioButtonKOREA = (RadioButton) findViewById(R.id.radioButtonKOREA);
		
		buttonSend.setOnClickListener(this);
		buttonClearSend.setOnClickListener(this);

		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);
	}

	@Override
	protected void onResume() {
		super.onResume();
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
		case R.id.buttonClearSend:
			editTextSend.setText("");
			break;
		case R.id.buttonSend: {
			// 不要直接和Pos打交道，要通过workThread来交流

			if (WorkService.workThread.isConnected()) {

				String text = editTextSend.getText().toString()
						+ "\r\n\r\n\r\n"; // 加三行换行，避免走纸
				byte header[] = null;
				byte strbuf[] = null;

				if (radioButtonGBK.isChecked()) {
					// 设置GBK编码
					// Android手机默认也是UTF8编码
					header = new byte[] { 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39,
							00 };
					try {
						strbuf = text.getBytes("GBK");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (radioButtonUTF8.isChecked()) {
					// 设置UTF8编码
					// Android手机默认也是UTF8编码
					header = new byte[] { 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39,
							0x01 };
					try {
						strbuf = text.getBytes("UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (radioButtonKOREA.isChecked()) {
					header = new byte[] { 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39,
							0x05 };
					try {
						strbuf = text.getBytes("euc-kr");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				byte buffer[] = DataUtils.byteArraysToBytes(new byte[][] {
						header, strbuf });
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buffer);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buffer.length);
				WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);

			} else {
				Toast.makeText(this, Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

		}
	}

	static class MHandler extends Handler {

		WeakReference<PlainTextActivity> mActivity;

		MHandler(PlainTextActivity activity) {
			mActivity = new WeakReference<PlainTextActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			PlainTextActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_POS_WRITERESULT: {
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
