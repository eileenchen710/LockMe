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
 * ���ð�ȫ�ֻ�������� ����Activity
 */

public class SettingSecurityActivity extends Activity {
	Activity thisAct;// ��ǰActivity
	Intent it;// �˻�������
	ConfigInfo configInfo;// ���ݽӿ�

	Button switchBt;// ���ð�ť
	EditText phoneNum;// �绰����
	boolean switchState = false;// ����״̬

	ImageView image;// ����ͼƬ

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// ��ʼ��
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

		// �����ʷ����
		if (configInfo.getPhoneNumber() != null)
			phoneNum.setText(configInfo.getPhoneNumber());

		// ��ť��Ӧ
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
			// ����ȷ�϶Ի���
			new AlertDialog.Builder(thisAct).setTitle("�������ã�")
					.setPositiveButton("ȡ��", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					}).setNegativeButton("ȷ��", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							configInfo.setPhoneNumber(phoneNum
									.getText().toString());
							configInfo.setGPSSwitch(switchState);
							Toast.makeText(thisAct, "����ɹ�", Toast.LENGTH_SHORT)
									.show();
							finish();
						}
					}).show();
		}

		return super.onKeyDown(keyCode, event);

	}

}
