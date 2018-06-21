package com.activity;

import com.data.ConfigInfo;
import com.lockscreen.R;
import com.service.LockingMeService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/*
 * ������ ����Activity
 */

public class PickingMeActivity extends Activity {

	Activity thisAct;// ��ǰActivity
	ConfigInfo configInfo;// ���ݽӿ�

	Button unlockBt;// ������ť
	Button cameraBt;
	Button phoneBt;
	Button messageBt;
	Button switchBt;
	Button securityBt;

	ImageView image;// ����ͼƬ

	boolean switchStatus = false;// ���񿪹�״̬

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ��ʼ��
		thisAct = this;
		setContentView(R.layout.activity_function_picking);

		image = (ImageView) this.findViewById(R.id.mainbg);

		unlockBt = (Button) findViewById(R.id.unlock);
		cameraBt = (Button) findViewById(R.id.camera);
		phoneBt = (Button) findViewById(R.id.phonecall);
		messageBt = (Button) findViewById(R.id.message);
		switchBt = (Button) findViewById(R.id.onoff);
		securityBt = (Button) findViewById(R.id.secure);

		configInfo = new ConfigInfo(this);
		switchStatus = configInfo.getSwitch(0);
		if (switchStatus) {
			image.setBackgroundResource(R.drawable.mainon);
			startService(new Intent(this, LockingMeService.class));
		} else
			image.setBackgroundResource(R.drawable.mainoff);

		// ������ť��Ӧ
		switchBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ���ؽ���
				if (!switchStatus) {
					boolean flag = false;
					for (int i = 1; i <= 4; i++) {
						if (configInfo.getSwitch(i)) {
							flag = true;
							break;
						}
					}
					if (flag) {
						startService(new Intent(thisAct, LockingMeService.class));
						image.setBackgroundResource(R.drawable.mainon);
						switchStatus = !switchStatus;
						configInfo.setSwitch(0, switchStatus);
					} else {
						// ���й��ܴ��ڹر�״̬
						Toast.makeText(thisAct, "���й��ܴ��ڹر�״̬������",
								Toast.LENGTH_SHORT).show();
					}

				}
				// ���ؽ��ر�
				else {
					stopService(new Intent(thisAct, LockingMeService.class));
					configInfo.setSwitch(0, false);
					image.setBackgroundResource(R.drawable.mainoff);
					switchStatus = !switchStatus;
				}
			}
		});

		// ������������˵��
		switchBt.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				new AlertDialog.Builder(thisAct).setTitle("ʹ��˵��")
						.setMessage(R.string.help_txt)
						.setPositiveButton("ȷ��", null).show();
				return false;
			}
		});

		unlockBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ��ת��¼�ƽ���
				Intent it = new Intent(thisAct, SettingMeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("function", 1);
				it.putExtras(bundle);
				startActivityForResult(it, 1);
			}
		});
		cameraBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ��ת��¼�ƽ���
				Intent it = new Intent(thisAct, SettingMeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("function", 2);
				it.putExtras(bundle);
				startActivityForResult(it, 1);
			}
		});
		phoneBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ��ת��¼�ƽ���
				Intent it = new Intent(thisAct, SettingMeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("function", 3);
				it.putExtras(bundle);
				startActivityForResult(it, 1);
			}
		});
		messageBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ��ת��¼�ƽ���
				Intent it = new Intent(thisAct, SettingMeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("function", 4);
				it.putExtras(bundle);
				startActivityForResult(it, 1);
			}
		});
		securityBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// ��ת����ȫ����
				Intent it = new Intent(thisAct, SettingSecurityActivity.class);
				startActivity(it);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			// ���й����ѹر� �رշ���
			stopService(new Intent(thisAct, LockingMeService.class));
			configInfo.setSwitch(0, false);
			image.setBackgroundResource(R.drawable.mainoff);
			switchStatus = !switchStatus;
		}

	}

}
