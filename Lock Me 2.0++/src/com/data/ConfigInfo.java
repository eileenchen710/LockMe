package com.data;

import android.app.Activity;
import android.content.SharedPreferences;

public class ConfigInfo {
	Activity myActivity;
	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;

	// ���캯�� �贫��һ��Activity������
	public ConfigInfo(Activity activity) {
		this.myActivity = activity;

		mySharedPreferences = myActivity.getSharedPreferences("data",
				Activity.MODE_APPEND);
		editor = mySharedPreferences.edit();
	}

	// �޸Ŀ���״̬
	public void setSwitch(int function, boolean on) {
		editor.putBoolean("switch-" + function, on);
		editor.commit();
	}

	// ��ÿ���״̬
	public boolean getSwitch(int function) {
		return mySharedPreferences.getBoolean("switch-" + function, false);
	}

	// �޸�GPS���񿪹�״̬
	public void setGPSSwitch(boolean on) {
		editor.putBoolean("switch-GPS", on);
		editor.commit();
	}

	// ���GPS���񿪹�״̬
	public boolean getGPSSwitch() {
		return mySharedPreferences.getBoolean("switch-GPS", false);
	}

	// ��ó�ʼ��״̬
	public boolean getInitState(int function) {
		return mySharedPreferences.getBoolean("init-" + function, false);
	}

	// ��������
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

	// ȡ���趨�Ľ���ҡ�β��� �޼�¼�򷵻�0
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

	// ������ϵ����
	public void setPhoneNumber(String number) {
		editor.putString("phone", number);
		editor.commit();
	}

	// �����ϵ����
	public String getPhoneNumber() {
		return mySharedPreferences.getString("phone", null);
	}

}
