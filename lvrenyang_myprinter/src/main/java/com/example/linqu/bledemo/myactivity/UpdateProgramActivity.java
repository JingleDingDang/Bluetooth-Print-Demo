package com.example.linqu.bledemo.myactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;
import com.example.linqu.bledemo.utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;


public class UpdateProgramActivity extends Activity implements OnClickListener {

	private static final String DIR_MAIN = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "MyPrinter"
			+ File.separator;
	private static final int REQUEST_CODE_SELECTFILE = 4;
	private static final String EXTRA_FILENAME = "extra.filename";

	private static ProgressBar progressBar;

	private static Handler mHandler = null;
	private static String TAG = "UpdateProgramActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updateprogram);

		findViewById(R.id.buttonUpdateProgram).setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBarUpdateProgress);

		// 建立目录
		File folder = new File(DIR_MAIN);
		if (!folder.exists())
			folder.mkdirs();

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
		case R.id.buttonUpdateProgram: {

			Intent intent = new Intent(this, FileManager.class);
			intent.putExtra(FileManager.EXTRA_INITIAL_DIRECTORY, DIR_MAIN);
			startActivityForResult(intent, REQUEST_CODE_SELECTFILE);

			break;
		}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (null == intent)
			return;

		switch (requestCode) {

		case REQUEST_CODE_SELECTFILE:
			String file = intent.getStringExtra(EXTRA_FILENAME);
			byte[] buffer = FileUtils.ReadToMem(file);
			if (null == buffer) {
				Toast.makeText(this, Global.toast_fail, Toast.LENGTH_LONG)
						.show();
				break;
			}
			// 不要直接和Pos打交道，要通过workThread来交流
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buffer);
				WorkService.workThread.handleCmd(Global.CMD_UPDATE_PROGRAM,
						data);
			} else {
				Toast.makeText(this, Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	static class MHandler extends Handler {

		WeakReference<UpdateProgramActivity> mActivity;

		MHandler(UpdateProgramActivity activity) {
			mActivity = new WeakReference<UpdateProgramActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			UpdateProgramActivity theActivity = mActivity.get();
			switch (msg.what) {

			case Global.CMD_UPDATE_PROGRAM_RESULT: {
				int result = msg.arg1;
				Toast.makeText(
						theActivity,
						(result == 1) ? Global.toast_success
								: Global.toast_fail, Toast.LENGTH_LONG).show();
				Log.v(TAG, "Result: " + result);
				break;
			}

			case Global.CMD_UPDATE_PROGRAM_PROGRESS: {
				int addr = msg.arg1;
				int total = msg.arg2;
				progressBar.setMax(total);
				progressBar.setProgress(addr);
				break;
			}

			}
		}
	}

}
