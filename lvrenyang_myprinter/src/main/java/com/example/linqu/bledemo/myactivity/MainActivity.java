package com.example.linqu.bledemo.myactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.linqu.bledemo.R;
import com.example.linqu.bledemo.myprinter.Global;
import com.example.linqu.bledemo.myprinter.WorkService;
import com.example.linqu.bledemo.utils.FileUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements OnClickListener {

	private ViewPager viewPager;// 页卡内容
	private ImageView imageView;// 动画图片
	private TextView textView1, textView2, textView3, textView4;
	private List<View> views;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private final int pageCount = 4;
	private View view1, view2, view3, view4;// 各个页卡
	private static Handler mHandler = null;
	private static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myprinter_main);
		InitImageView();
		InitTextView();
		InitViewPager();

		// 初始化字符串资源
		InitGlobalString();

		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);

		if (null == WorkService.workThread) {
			Intent intent = new Intent(this, WorkService.class);
			startService(intent);
		}
		
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// remove the handler
		WorkService.delHandler(mHandler);
		mHandler = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (WorkService.workThread.isConnecting()) {
			Toast.makeText(this, "please waiting for connecting finished!",
					Toast.LENGTH_SHORT).show();
			return true;
		}

		switch (item.getItemId()) {

		case R.id.menu_exit:
			stopService(new Intent(this, WorkService.class));
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void InitGlobalString() {
		Global.toast_success = getString(R.string.toast_success);
		Global.toast_fail = getString(R.string.toast_fail);
		Global.toast_notconnect = getString(R.string.toast_notconnect);
		Global.toast_usbpermit = getString(R.string.toast_usbpermit);
	}

	@SuppressLint("InflateParams")
	private void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.vPager);
		views = new ArrayList<View>();
		LayoutInflater inflater = getLayoutInflater();
		view1 = inflater.inflate(R.layout.lay1, null);
		view2 = inflater.inflate(R.layout.lay2, null);
		view3 = inflater.inflate(R.layout.lay3, null);
		view4 = inflater.inflate(R.layout.lay4, null);
		view1.findViewById(R.id.btPicture).setOnClickListener(this);
		view1.findViewById(R.id.btBWPicture).setOnClickListener(this);
		view1.findViewById(R.id.btCurve).setOnClickListener(this);
		view1.findViewById(R.id.btFormatText).setOnClickListener(this);
		view1.findViewById(R.id.btPlainText).setOnClickListener(this);
		view1.findViewById(R.id.btForm).setOnClickListener(this);
		view1.findViewById(R.id.btBarcode).setOnClickListener(this);
		view1.findViewById(R.id.btQrcode).setOnClickListener(this);
		view2.findViewById(R.id.btWebPrint).setOnClickListener(this);
		view2.findViewById(R.id.btBrowserPrint).setOnClickListener(this);
		view3.findViewById(R.id.btSendCmd).setOnClickListener(this);
		view3.findViewById(R.id.btCutterCmd).setOnClickListener(this);
		view3.findViewById(R.id.btTC03).setOnClickListener(this);
		view3.findViewById(R.id.btSetKey).setOnClickListener(this);
		view3.findViewById(R.id.btCheckKey).setOnClickListener(this);
		view4.findViewById(R.id.btConnectPrinterMac).setOnClickListener(this);
		view4.findViewById(R.id.btConnectPrinterPaired)
				.setOnClickListener(this);
		view4.findViewById(R.id.btConnectPrinterSearched).setOnClickListener(
				this);
		view4.findViewById(R.id.btConnectIP).setOnClickListener(this);
		view4.findViewById(R.id.btConnectUSB).setOnClickListener(this);
		view4.findViewById(R.id.btSetPrinterPara).setOnClickListener(this);
		view4.findViewById(R.id.btConnectBLE).setOnClickListener(this);
		view4.findViewById(R.id.btDisconnect).setOnClickListener(this);
		view4.findViewById(R.id.btExit).setOnClickListener(this);
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		viewPager.setAdapter(new MyViewPagerAdapter(views));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 初始化头标
	 */

	private void InitTextView() {
		textView1 = (TextView) findViewById(R.id.text1);
		textView2 = (TextView) findViewById(R.id.text2);
		textView3 = (TextView) findViewById(R.id.text3);
		textView4 = (TextView) findViewById(R.id.text4);
		textView1.setOnClickListener(new MyOnClickListener(0));
		textView2.setOnClickListener(new MyOnClickListener(1));
		textView3.setOnClickListener(new MyOnClickListener(2));
		textView4.setOnClickListener(new MyOnClickListener(3));

	}

	/***
	 * 初始化动画
	 */
	private void InitImageView() {
		imageView = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / pageCount - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);// 设置动画初始位置
	}

	/***
	 * 头标点击监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}

	}

	public class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(one * currIndex, one
					* arg0, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			imageView.startAnimation(animation);
			Log.v(TAG, "您选择了" + viewPager.getCurrentItem() + "页卡");
		}

	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onClick:" + arg0.toString());
		switch (arg0.getId()) {
		case R.id.btPicture:
			startActivity(new Intent(this, PictureActivity.class));
			break;
		case R.id.btBWPicture:
			startActivity(new Intent(this, BWPicActivity.class));
			break;
		case R.id.btCurve:
			startActivity(new Intent(this, CurveActivity.class));
			break;
		case R.id.btFormatText:
			startActivity(new Intent(this, FormatTextActivity.class));
			break;
		case R.id.btPlainText:
			startActivity(new Intent(this, PlainTextActivity.class));
			break;
		case R.id.btForm:
			startActivity(new Intent(this, FormActivity.class));
			break;
		case R.id.btBarcode:
			startActivity(new Intent(this, BarcodeActivity.class));
			break;
		case R.id.btQrcode:
			startActivity(new Intent(this, QrcodeActivity.class));
			break;
		case R.id.btWebPrint:
			startActivity(new Intent(this, JSAndroidActivity.class));
			break;
		case R.id.btCutterCmd:
			startActivity(new Intent(this, CutterActivity.class));
			break;
		case R.id.btConnectPrinterMac:
			startActivity(new Intent(this, ConnectBTMacActivity.class));
			break;
		case R.id.btConnectPrinterPaired:
			startActivity(new Intent(this, ConnectBTPairedActivity.class));
			break;
		case R.id.btConnectBLE:
			startActivity(new Intent(this, SearchBLEActivity.class));
			break;
		case R.id.btConnectPrinterSearched:
			startActivity(new Intent(this, SearchBTActivity.class));
			break;
		case R.id.btConnectIP:
			startActivity(new Intent(this, ConnectIPActivity.class));
			break;
		case R.id.btConnectUSB:
			startActivity(new Intent(this, ConnectUSBActivity.class));
			break;
		case R.id.btSetKey:
			startActivity(new Intent(this, SetKeyActivity.class));
			break;
		case R.id.btCheckKey:
			startActivity(new Intent(this, CheckKeyActivity.class));
			break;
		case R.id.btDisconnect:
			WorkService.workThread.disconnectBle();
			WorkService.workThread.disconnectBt();
			WorkService.workThread.disconnectNet();
			WorkService.workThread.disconnectUsb();
			break;
		case R.id.btExit:
			stopService(new Intent(this, WorkService.class));
			finish();
			break;
		}
	}

	static class MHandler extends Handler {

		WeakReference<MainActivity> mActivity;

		MHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity theActivity = mActivity.get();
			switch (msg.what) {

			}
		}
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			} else {
				handleSendRaw(intent);
			}
		}
	}

	private void handleSendText(Intent intent) {
		Uri textUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (textUri != null) {
			// Update UI to reflect text being shared

			if (WorkService.workThread.isConnected()) {
				byte[] buffer = { 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39, 0x01 }; // 设置中文，切换双字节编码。
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buffer);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buffer.length);
				WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
			}
			if (WorkService.workThread.isConnected()) {
				String path = textUri.getPath();
				String strText = FileUtils.ReadToString(path);
				byte buffer[] = strText.getBytes();

				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buffer);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buffer.length);
				data.putInt(Global.INTPARA3, 128);
				WorkService.workThread.handleCmd(
						Global.CMD_POS_WRITE_BT_FLOWCONTROL, data);

			} else {
				Toast.makeText(this, Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}

			finish();
		}
	}

	private void handleSendRaw(Intent intent) {
		Uri textUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (textUri != null) {
			// Update UI to reflect text being shared
			if (WorkService.workThread.isConnected()) {
				String path = textUri.getPath();
				byte buffer[] = FileUtils.ReadToMem(path);
				// Toast.makeText(this, "length:" + buffer.length,
				// Toast.LENGTH_LONG).show();
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buffer);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buffer.length);
				data.putInt(Global.INTPARA3, 256);
				WorkService.workThread.handleCmd(
						Global.CMD_POS_WRITE_BT_FLOWCONTROL, data);

			} else {
				Toast.makeText(this, Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}

			// finish();
		}
	}

	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			String path = getRealPathFromURI(imageUri);

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			opts.inJustDecodeBounds = false;
			if (opts.outWidth > 1200) {
				opts.inSampleSize = opts.outWidth / 1200;
			}

			Bitmap mBitmap = BitmapFactory.decodeFile(path);

			if (mBitmap != null) {
				if (WorkService.workThread.isConnected()) {
					Bundle data = new Bundle();
					data.putParcelable(Global.PARCE1, mBitmap);
					data.putInt(Global.INTPARA1, 384);
					data.putInt(Global.INTPARA2, 0);
					WorkService.workThread.handleCmd(
							Global.CMD_POS_PRINTPICTURE, data);
				} else {
					Toast.makeText(this, Global.toast_notconnect,
							Toast.LENGTH_SHORT).show();
				}
			}
			finish();
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}
}
