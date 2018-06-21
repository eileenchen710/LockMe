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
 * 锁屏Activity
 */
public class LockingMeActivity extends Activity implements SensorEventListener {
	Activity thisAct;// 当前Activity

	SensorManager sensorManager;// 传感器
	ConfigInfo configInfo;// 数据接口
	ArrayList<int[]> totalInput;// 本次输入
	int totalCount = 0;// 总大小
	int input[][];// 本次输入样本
	int sampleCount = 20;// 样本大小
	int limit = 3;// 敏感度(超过本值才视为有效运动

	long screenTime = 0;// 状态持续时间
	boolean waitForWakingStatus = false;// 灭屏状态
	int attemptCount = 0;// 连续失败次数

	Button setBt;// 录制按钮
	boolean setStatus;// 录制状态
	boolean gpsStatus;// 安全功能状态

	Button bugBt;// 测试用紧急退出按钮

	ImageView image;// 按下时显示图片

	int callType = 0;// 被调用的形式：1 for 解锁；2 for 验证
	int function = 0;// 验证密码的功能

	// 地址客户端
	String sendMobile = "+8615913134101";
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	// 短信
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
			// 参数初始化
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

		// 界面初始化
		setContentView(R.layout.activity_lock_locking);
		callType = getIntent().getExtras().getInt("call_type");
		if (1 == callType) {
			// 解锁界面
			image = (ImageView) this.findViewById(R.id.time);
			image.setBackgroundResource(R.drawable.time);

			if (gpsStatus) {
				// 位置客户端初始化
				mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
				mLocationClient.registerLocationListener(myListener); // 注册监听函数
				// 设置位置参数
				setLocationOption();
			}

		} else if (2 == callType) {
			// 确认密码界面
			function = getIntent().getExtras().getInt("function");
			image = (ImageView) this.findViewById(R.id.time);
			image.setBackgroundResource(R.drawable.verify);
		}

		// 测试用紧急退出按钮
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
		// 录制按钮
		setBt.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {

				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// 按下后换背景
					image.setBackgroundResource(R.drawable.unlock);

					// 清空记录数据
					setStatus = true;
					totalInput = new ArrayList<int[]>();
					totalCount = 0;
					break;
				}
				case MotionEvent.ACTION_UP: {
					if (1 == callType) {
						// 解锁界面
						image.setBackgroundResource(R.drawable.time);

					} else if (2 == callType) {
						// 确认密码界面
						image.setBackgroundResource(R.drawable.verify);
					}
					setStatus = false;

					// 未获得输入数据
					if (totalCount == 0)
						break;

					// 取样
					input = MyAlgorithm.sampling(totalInput, sampleCount);

					// 与设定的密码进行比较
					if (1 == callType) {
						// 解锁
						switch (compareKeys(input)) {
						case 0: {
							// 无匹配
							Toast.makeText(thisAct, "密码错误 请重试",
									Toast.LENGTH_SHORT).show();

							if (gpsStatus) {
								if (attemptCount > -1)
									attemptCount++;

								if (attemptCount >= 5) {
									// 超过五次 发送位置信息短信
									Toast.makeText(
											thisAct,
											"已连续五次解锁失败\n将每隔1分钟发送一次位置\n成功解锁即可解除",
											Toast.LENGTH_LONG).show();
									// 获取位置
									mLocationClient.start();

									// 发起定位请求
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
							// 解锁
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

							// 调用相机
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

							// 调用拨号
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

							// 调用短信
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
						// 验证
						if (MyAlgorithm.compareKeys(input,
								configInfo.getPassword(function))) {
							// 验证通过
							Intent temp = new Intent();
							setResult(1, temp);

							sensorManager
									.unregisterListener((LockingMeActivity) thisAct);
							finish();
						} else {
							Toast.makeText(thisAct, "密码错误 请重试",
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
			// 屏蔽HOME键
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (1 == callType) {
				// 解锁界面禁用
				return true;
			} else {
				// check界面
				Intent temp = new Intent();
				setResult(2, temp);// 验证失败
				sensorManager.unregisterListener((LockingMeActivity) thisAct);
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		// 传感器数据处理
		case Sensor.TYPE_LINEAR_ACCELERATION: {
			// 线性传感器 解锁用
			if (!setStatus)
				return;

			boolean flag = false;// 本次运动是否有效
			float[] temp = new float[3];// 传感器本次获得的数据
			int[] tempInput = new int[3];// 本次取得的运动数据
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
			// 距离传感器 唤醒屏幕用
			float temp = event.values[0];
			if (temp < 0.5) {
				if (!waitForWakingStatus) {
					// 未进入口袋模式
					if (screenTime == 0) {
						// 开始计时
						screenTime = System.currentTimeMillis();
					} else {
						// 计时中
						if (System.currentTimeMillis() - screenTime > 5) {
							// 近距状态超过五秒 进入口袋
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
				// 亮屏
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

	// 当前输入的密码与原先的密码逐一进行比较
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

	// 设置定位参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll");// 设置坐标类型
		option.setServiceName("com.baidu.location.service_v2.9");
		option.setPoiExtraInfo(true);
		option.setAddrType("all"); // 设置显示地址
		option.setScanSpan(60000); // 设置每隔1分钟进行一次定位
		option.setPriority(LocationClientOption.GpsFirst); // 优先GPS定位
		option.setPoiNumber(10);
		option.disableCache(true);
		mLocationClient.setLocOption(option); // mLocationClient为Location类的实例化对象
	}

	// 获取位置及发送短信
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			sb.append("经度 : ");
			sb.append(location.getLatitude());
			sb.append("\n纬度 : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\n地址 : ");
				sb.append(location.getAddrStr());

			}
			// 发送信息
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