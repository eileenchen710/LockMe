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
 * 主界面 设置Activity
 */
public class SettingMeActivity extends Activity implements SensorEventListener {
	Activity thisAct;

	SensorManager sensorManager;// 传感器
	ConfigInfo configInfo;// 数据接口

	ArrayList<int[]> totalInput;// 输入
	int totalCount = 0;// 总大小
	int input[][];// 输入样本
	int sampleCount = 20;// 样本大小
	int limit = 3;// 敏感度(超过本值才视为有效运动

	Button setBt;// 录制按钮
	boolean setStatus;// 录制状态
	Button switchBt;// 开关按钮
	boolean switchStatus;// 服务开关状态

	int function;// 设置的功能
	ImageView image;// 背景图片

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 获得上一个activity传来的功能编号
		Bundle bundle = getIntent().getExtras();
		function = bundle.getInt("function");

		// 初始化
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

		// 开关按钮
		switchBt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!configInfo.getInitState(function)) {
					// 未设定密码
					Toast.makeText(thisAct, "请先设定密码", Toast.LENGTH_SHORT)
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

		// 录制按钮
		setBt.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					image.setBackgroundResource(R.drawable.press);
					// 清空记录数据
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

					// 未获得输入数据
					if (totalCount == 0) {
						Toast.makeText(thisAct, "未获得数据 请重新录制",
								Toast.LENGTH_SHORT).show();
						break;
					}

					// 弹出确认对话框
					new AlertDialog.Builder(thisAct).setTitle("保存密码？")
							.setPositiveButton("取消", null)
							.setNegativeButton("确认", new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									input = MyAlgorithm.sampling(totalInput,
											sampleCount);

									// 相似判断
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
												"与其他密码过于相似 请重新录制",
												Toast.LENGTH_SHORT).show();

									} else {
										configInfo.setData(function, input);
										Toast.makeText(thisAct, "密码修改成功",
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
			// 如果已存在密码 打开Activity时先弹出验证Activity
			Intent checkIntent = new Intent(thisAct, LockingMeActivity.class);
			checkIntent.putExtra("call_type", 2);
			checkIntent.putExtra("function", function);
			startActivityForResult(checkIntent, 2);
			Toast.makeText(thisAct, "请先输入当前密码", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!setStatus)
			return;

		switch (event.sensor.getType()) {
		// 传感器数据处理
		case Sensor.TYPE_LINEAR_ACCELERATION: {
			// 线性加速度
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
			// 检查是否所有功能都已关闭
			boolean flag = true;
			for (int i = 1; i <= 4; i++) {
				if (configInfo.getSwitch(i)) {
					flag = false;
				}
			}
			if (flag) {
				// 功能全部关闭
				Intent temp = new Intent();
				setResult(1, temp);// 向PickingMeActivity返回参数
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// 从LockingMeActivity获得验证返回值
		switch (resultCode) {
		case 1: {
			// 解锁成功
			break;
		}
		case 2: {
			// 解锁失败
			finish();
			break;
		}
		default:
			break;
		}
	}
}
