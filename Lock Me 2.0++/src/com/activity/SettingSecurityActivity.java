package com.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.data.ConfigInfo;
import com.lockscreen.R;

/*
 * 设置安全手机号码界面 设置Activity
 */

public class SettingSecurityActivity extends Activity {
	Activity thisAct;// 当前Activity
	Intent it;// 退回主界面
	ConfigInfo configInfo;// 数据接口

	Button switchBt;// 设置按钮
	EditText phoneNum;// 电话号码
	boolean switchState = false;// 开关状态

	ImageView image;// 背景图片

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// 初始化
		thisAct = this;
		setContentView(R.layout.activity_secure_setting);
		configInfo = new ConfigInfo(this);

		image = (ImageView) this.findViewById(R.id.securebg);
		switchBt = (Button) findViewById(R.id.set);
		phoneNum = (EditText) findViewById(R.id.phone);

		phoneNum.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });

		switchState = configInfo.getGPSSwitch();
		if (switchState) {
			image.setBackgroundResource(R.drawable.secureon);
		} else {
			image.setBackgroundResource(R.drawable.secureoff);
		}

		// 获得历史号码
		if (configInfo.getPhoneNumber() != null)
			phoneNum.setText(configInfo.getPhoneNumber());

		// 按钮响应
		switchBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchState = !switchState;
				if (switchState) {
					image.setBackgroundResource(R.drawable.secureon);
				} else {
					image.setBackgroundResource(R.drawable.secureoff);
				}
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 弹出确认对话框
			new AlertDialog.Builder(thisAct).setTitle("保存设置？")
					.setPositiveButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					}).setNegativeButton("确认", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							configInfo.setPhoneNumber(phoneNum
									.getText().toString());
							configInfo.setGPSSwitch(switchState);
							Toast.makeText(thisAct, "保存成功", Toast.LENGTH_SHORT)
									.show();
							finish();
						}
					}).show();
		}

		return super.onKeyDown(keyCode, event);

	}

}
