package com.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.data.ConfigInfo;
import com.data.MyAlgorithm;
import com.lockscreen.R;

/*
 * ������ ����Activity
 */
public class SettingMeActivity extends Activity implements SensorEventListener {
	Activity thisAct;

	SensorManager sensorManager;// ������
	ConfigInfo configInfo;// ���ݽӿ�

	ArrayList<int[]> totalInput;// ����
	int totalCount = 0;// �ܴ�С
	int input[][];// ��������
	int sampleCount = 20;// ������С
	int limit = 3;// ���ж�(������ֵ����Ϊ��Ч�˶�

	Button setBt;// ¼�ư�ť
	boolean setStatus;// ¼��״̬
	Button switchBt;// ���ذ�ť
	boolean switchStatus;// ���񿪹�״̬

	int function;// ���õĹ���
	ImageView image;// ����ͼƬ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// �����һ��activity�����Ĺ��ܱ��
		Bundle bundle = getIntent().getExtras();
		function = bundle.getInt("function");

		// ��ʼ��
		thisAct = this;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager
				.registerListener(this, sensorManager
						.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
						SensorManager.SENSOR_DELAY_GAME);
		configInfo = new ConfigInfo(this);
		setContentView(R.layout.activity_lock_setting);

		image = (ImageView) this.findViewById(R.id.set);
		image.setBackgroundResource(R.drawable.off);

		setBt = (Button) findViewById(R.id.setbt);
		setStatus = false;

		switchBt = (Button) findViewById(R.id.switchTBT);
		switchStatus = configInfo.getSwitch(function);
		if (switchStatus) {
			image.setBackgroundResource(R.drawable.on);
		} else {
			image.setBackgroundResource(R.drawable.off);
		}

		// ���ذ�ť
		switchBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!configInfo.getInitState(function)) {
					// δ�趨����
					Toast.makeText(thisAct, "�����趨����", Toast.LENGTH_SHORT)
							.show();
				} else {
					if (switchStatus) {
						image.setBackgroundResource(R.drawable.off);
						configInfo.setSwitch(function, false);

					} else {
						image.setBackgroundResource(R.drawable.on);
						configInfo.setSwitch(function, true);
					}
					switchStatus = !switchStatus;
					configInfo.setSwitch(function, switchStatus);
				}
			}
		});

		// ¼�ư�ť
		setBt.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					image.setBackgroundResource(R.drawable.press);
					// ��ռ�¼����
					setStatus = true;
					totalInput = new ArrayList<int[]>();
					totalCount = 0;

					break;
				}
				case MotionEvent.ACTION_UP: {
					if (switchStatus)
						image.setBackgroundResource(R.drawable.on);
					else
						image.setBackgroundResource(R.drawable.off);

					setStatus = false;

					// δ�����������
					if (totalCount == 0) {
						Toast.makeText(thisAct, "δ������� ������¼��",
								Toast.LENGTH_SHORT).show();
						break;
					}

					// ����ȷ�϶Ի���
					new AlertDialog.Builder(thisAct).setTitle("�������룿")
							.setPositiveButton("ȡ��", null)
							.setNegativeButton("ȷ��", new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									input = MyAlgorithm.sampling(totalInput,
											sampleCount);

									// �����ж�
									boolean flag = false;
									for (int i = 1; i < 4; i++) {
										if (i != function
												&& MyAlgorithm
														.compareSimilarity(
																input,
																configInfo
																		.getPassword(i))) {
											flag = true;
											break;
										}
									}
									if (flag) {
										Toast.makeText(thisAct,
												"����������������� ������¼��",
												Toast.LENGTH_SHORT).show();

									} else {
										configInfo.setData(function, input);
										Toast.makeText(thisAct, "�����޸ĳɹ�",
												Toast.LENGTH_SHORT).show();
									}
								}
							}).show();
					break;
				}

				default:
					break;
				}
				return false;
			}
		});

		if (configInfo.getInitState(function)) {
			// ����Ѵ������� ��Activityʱ�ȵ�����֤Activity
			Intent checkIntent = new Intent(thisAct, LockingMeActivity.class);
			checkIntent.putExtra("call_type", 2);
			checkIntent.putExtra("function", function);
			startActivityForResult(checkIntent, 2);
			Toast.makeText(thisAct, "�������뵱ǰ����", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!setStatus)
			return;

		switch (event.sensor.getType()) {
		// ���������ݴ���
		case Sensor.TYPE_LINEAR_ACCELERATION: {
			// ���Լ��ٶ�
			boolean flag = false;
			float[] temp = new float[3];
			int[] tempInput = new int[3];
			temp = event.values.clone();
			for (int i = 0; i < 3; i++) {
				if (temp[i] > limit) {
					tempInput[i] = 1;
					flag = true;
				} else if (temp[i] < -limit) {
					tempInput[i] = -1;
					flag = true;
				} else {
					tempInput[i] = 0;
				}
			}
			if (flag) {
				totalInput.add(tempInput);
				totalCount++;
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// ����Ƿ����й��ܶ��ѹر�
			boolean flag = true;
			for (int i = 1; i <= 4; i++) {
				if (configInfo.getSwitch(i)) {
					flag = false;
				}
			}
			if (flag) {
				// ����ȫ���ر�
				Intent temp = new Intent();
				setResult(1, temp);// ��PickingMeActivity���ز���
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// ��LockingMeActivity�����֤����ֵ
		switch (resultCode) {
		case 1: {
			// �����ɹ�
			break;
		}
		case 2: {
			// ����ʧ��
			finish();
			break;
		}
		default:
			break;
		}
	}
}
