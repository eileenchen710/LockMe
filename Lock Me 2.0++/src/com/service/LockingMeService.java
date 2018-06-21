package com.service;

import com.receiver.LockingMeReceiver;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/*
 * 服务类 开启广播接收器
 */
public class LockingMeService extends Service {
	BroadcastReceiver mReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		KeyguardManager.KeyguardLock k1;

		KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		k1 = km.newKeyguardLock("IN");
		k1.disableKeyguard();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);

		mReceiver = new LockingMeReceiver();
		registerReceiver(mReceiver, filter);

		super.onCreate();

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub

		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
