package com.activity;

import java.util.ArrayList;

import com.lockscreen.R;
import com.data.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/*
 * ����Activity
 */
public class LockingMeActivity extends Activity implements SensorEventListener {
	Activity thisAct;// ��ǰActivity

	SensorManager sensorManager;// ������
	ConfigInfo configInfo;// ���ݽӿ�
	ArrayList<int[]> totalInput;// ��������
	int totalCount = 0;// �ܴ�С
	int input[][];// ������������
	int sampleCount = 20;// ������С
	int limit = 3;// ���ж�(������ֵ����Ϊ��Ч�˶�

	long screenTime = 0;// ״̬����ʱ��
	boolean waitForWakingStatus = false;// ����״̬
	int attemptCount = 0;// ����ʧ�ܴ���

	Button setBt;// ¼�ư�ť
	boolean setStatus;// ¼��״̬
	boolean gpsStatus;// ��ȫ����״̬

	Button bugBt;// �����ý����˳���ť

	ImageView image;// ����ʱ��ʾͼƬ

	int callType = 0;// �����õ���ʽ��1 for ������2 for ��֤
	int function = 0;// ��֤����Ĺ���

	// ��ַ�ͻ���
	String sendMobile = "+8615913134101";
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	// ����
	SmsManager smsManager;

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onAttachedToWindow();
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		try {
			// ������ʼ��
			thisAct = this;
			configInfo = new ConfigInfo(this);
			gpsStatus = configInfo.getGPSSwitch();
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.registerListener(this, sensorManager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
					SensorManager.SENSOR_DELAY_GAME);
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
					SensorManager.SENSOR_DELAY_NORMAL);

		} catch (Exception e) {
		}

		// �����ʼ��
		setContentView(R.layout.activity_lock_locking);
		callType = getIntent().getExtras().getInt("call_type");
		if (1 == callType) {
			// ��������
			image = (ImageView) this.findViewById(R.id.time);
			image.setBackgroundResource(R.drawable.time);

			if (gpsStatus) {
				// λ�ÿͻ��˳�ʼ��
				mLocationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
				mLocationClient.registerLocationListener(myListener); // ע���������
				// ����λ�ò���
				setLocationOption();
			}

		} else if (2 == callType) {
			// ȷ���������
			function = getIntent().getExtras().getInt("function");
			image = (ImageView) this.findViewById(R.id.time);
			image.setBackgroundResource(R.drawable.verify);
		}

		// �����ý����˳���ť
		{
			bugBt = (Button) findViewById(R.id.bugbt);
			bugBt.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					if (2 == callType) {
						Intent temp = new Intent();
						setResult(1, temp);
					} else if (gpsStatus) {
						attemptCount = 0;
						mLocationClient.stop();
					}
					sensorManager
							.unregisterListener((LockingMeActivity) thisAct);
					finish();
					return false;
				}
			});
		}

		setBt = (Button) findViewById(R.id.setbt);
		// ¼�ư�ť
		setBt.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {

				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// ���º󻻱���
					image.setBackgroundResource(R.drawable.unlock);

					// ��ռ�¼����
					setStatus = true;
					totalInput = new ArrayList<int[]>();
					totalCount = 0;
					break;
				}
				case MotionEvent.ACTION_UP: {
					if (1 == callType) {
						// ��������
						image.setBackgroundResource(R.drawable.time);

					} else if (2 == callType) {
						// ȷ���������
						image.setBackgroundResource(R.drawable.verify);
					}
					setStatus = false;

					// δ�����������
					if (totalCount == 0)
						break;

					// ȡ��
					input = MyAlgorithm.sampling(totalInput, sampleCount);

					// ���趨��������бȽ�
					if (1 == callType) {
						// ����
						switch (compareKeys(input)) {
						case 0: {
							// ��ƥ��
							Toast.makeText(thisAct, "������� ������",
									Toast.LENGTH_SHORT).show();

							if (gpsStatus) {
								if (attemptCount > -1)
									attemptCount++;

								if (attemptCount >= 5) {
									// ������� ����λ����Ϣ����
									Toast.makeText(
											thisAct,
											"��������ν���ʧ��\n��ÿ��1���ӷ���һ��λ��\n�ɹ��������ɽ��",
											Toast.LENGTH_LONG).show();
									// ��ȡλ��
									mLocationClient.start();

									// ����λ����
									if (mLocationClient != null
											&& mLocationClient.isStarted()) {
										mLocationClient.requestLocation();
									} else
										Log.d("LocSDK3",
												"locClient is null or not started");

									attemptCount = -1;
								}
							}

							break;
						}
						case 1: {
							// ����
							if (gpsStatus) {
								attemptCount = 0;
								mLocationClient.stop();
							}
							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
							break;
						}
						case 2: {
							if (gpsStatus) {
								attemptCount = 0;
								mLocationClient.stop();
							}

							// �������
							Intent intent = new Intent(
									"android.media.action.STILL_IMAGE_CAMERA");
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							startActivity(intent);
							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
							break;
						}
						case 3: {
							if (gpsStatus) {
								attemptCount = 0;
								mLocationClient.stop();
							}

							// ���ò���
							Intent intent = new Intent(
									"android.intent.action.CALL_BUTTON");
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							startActivity(intent);
							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
							break;
						}
						case 4: {
							if (gpsStatus) {
								attemptCount = 0;
								mLocationClient.stop();
							}

							// ���ö���
							Intent intent = new Intent(Intent.ACTION_MAIN);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.setType("vnd.android-dir/mms-sms");
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							startActivity(intent);
							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
							break;
						}
						default:
							break;
						}
					} else if (2 == callType) {
						// ��֤
						if (MyAlgorithm.compareKeys(input,
								configInfo.getPassword(function))) {
							// ��֤ͨ��
							Intent temp = new Intent();
							setResult(1, temp);

							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
						} else {
							Toast.makeText(thisAct, "������� ������",
									Toast.LENGTH_SHORT).show();
						}
					}
					break;
				}

				default:
					break;
				}
				return false;
			}
		});

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			// ����HOME��
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (1 == callType) {
				// �����������
				return true;
			} else {
				// check����
				Intent temp = new Intent();
				setResult(2, temp);// ��֤ʧ��
				sensorManager.unregisterListener((LockingMeActivity) thisAct);
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		// ���������ݴ���
		case Sensor.TYPE_LINEAR_ACCELERATION: {
			// ���Դ����� ������
			if (!setStatus)
				return;

			boolean flag = false;// �����˶��Ƿ���Ч
			float[] temp = new float[3];// ���������λ�õ�����
			int[] tempInput = new int[3];// ����ȡ�õ��˶�����
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
		case Sensor.TYPE_PROXIMITY: {
			// ���봫���� ������Ļ��
			float temp = event.values[0];
			if (temp < 0.5) {
				if (!waitForWakingStatus) {
					// δ����ڴ�ģʽ
					if (screenTime == 0) {
						// ��ʼ��ʱ
						screenTime = System.currentTimeMillis();
					} else {
						// ��ʱ��
						if (System.currentTimeMillis() - screenTime > 5) {
							// ����״̬�������� ����ڴ�
							waitForWakingStatus = true;
							screenTime = 0;
						}
					}
				}
			} else if (waitForWakingStatus) {
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				@SuppressWarnings("deprecation")
				PowerManager.WakeLock mWakeLock = pm.newWakeLock(
						PowerManager.ACQUIRE_CAUSES_WAKEUP
								| PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "test");
				// ����
				mWakeLock.acquire();
				mWakeLock.release();
				waitForWakingStatus = false;
			}
			break;
		}
		default:
			break;
		}
	}

	// ��ǰ�����������ԭ�ȵ�������һ���бȽ�
	public int compareKeys(int[][] input) {
		for (int i = 1; i <= 4; i++) {
			if (configInfo.getSwitch(i)
					&& MyAlgorithm
							.compareKeys(input, configInfo.getPassword(i))) {
				return i;
			}
		}
		return 0;
	}

	// ���ö�λ����
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll");// ������������
		option.setServiceName("com.baidu.location.service_v2.9");
		option.setPoiExtraInfo(true);
		option.setAddrType("all"); // ������ʾ��ַ
		option.setScanSpan(60000); // ����ÿ��1���ӽ���һ�ζ�λ
		option.setPriority(LocationClientOption.GpsFirst); // ����GPS��λ
		option.setPoiNumber(10);
		option.disableCache(true);
		mLocationClient.setLocOption(option); // mLocationClientΪLocation���ʵ��������
	}

	// ��ȡλ�ü����Ͷ���
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			sb.append("���� : ");
			sb.append(location.getLatitude());
			sb.append("\nγ�� : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\n��ַ : ");
				sb.append(location.getAddrStr());

			}
			// ������Ϣ
			sendMobile = configInfo.getPhoneNumber();
			smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(sendMobile, null, sb.toString(), null,
					null);

		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
		}

	}

}