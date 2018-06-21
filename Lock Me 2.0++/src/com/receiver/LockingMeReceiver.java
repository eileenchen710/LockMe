package com.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.activity.LockingMeActivity;

/*
 * 广播接收器 处理屏幕点亮/熄灭&重启
 */
public class LockingMeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Intent intent11 = new Intent(context, LockingMeActivity.class);
			intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent11.putExtra("call_type", 1);
			context.startActivity(intent11);

		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Intent intent11 = new Intent(context, LockingMeActivity.class);
			intent11.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent11.putExtra("call_type", 1);
			

		} else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent intent11 = new Intent(context, LockingMeActivity.class);
			intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent11.putExtra("call_type", 1);
			context.startActivity(intent11);
		}

	}

}
