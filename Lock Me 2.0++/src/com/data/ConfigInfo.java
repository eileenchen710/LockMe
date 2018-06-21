package com.data;

import android.app.Activity;
import android.content.SharedPreferences;

public class ConfigInfo {
	Activity myActivity;
	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;

	// 构造函数 需传入一个Activity作参数
	public ConfigInfo(Activity activity) {
		this.myActivity = activity;

		mySharedPreferences = myActivity.getSharedPreferences("data",
				Activity.MODE_APPEND);
		editor = mySharedPreferences.edit();
	}

	// 修改开关状态
	public void setSwitch(int function, boolean on) {
		editor.putBoolean("switch-" + function, on);
		editor.commit();
	}

	// 获得开关状态
	public boolean getSwitch(int function) {
		return mySharedPreferences.getBoolean("switch-" + function, false);
	}

	// 修改GPS服务开关状态
	public void setGPSSwitch(boolean on) {
		editor.putBoolean("switch-GPS", on);
		editor.commit();
	}

	// 获得GPS服务开关状态
	public boolean getGPSSwitch() {
		return mySharedPreferences.getBoolean("switch-GPS", false);
	}

	// 获得初始化状态
	public boolean getInitState(int function) {
		return mySharedPreferences.getBoolean("init-" + function, false);
	}

	// 存入数据
	public void setData(int function, int[][] password) {
		editor.putBoolean("init-" + function, true);

		for (int i = 0; i < password.length; i++) {
			for (int j = 0; j < 3; j++) {
				editor.putInt("password-" + function + "-" + i + "-" + j,
						password[i][j]);
			}
		}
		editor.commit();
	}

	// 取得设定的解锁摇晃参数 无记录则返回0
	public int[][] getPassword(int function) {
		int[][] password = new int[20][3];
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 3; j++) {
				password[i][j] = mySharedPreferences.getInt("password-"
						+ function + "-" + i + "-" + j, 0);
			}
		}
		return password;
	}

	// 存入联系号码
	public void setPhoneNumber(String number) {
		editor.putString("phone", number);
		editor.commit();
	}

	// 获得联系号码
	public String getPhoneNumber() {
		return mySharedPreferences.getString("phone", null);
	}

}
