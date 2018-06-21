package com.data;

import java.util.ArrayList;

public class MyAlgorithm {

	static public int[][] sampling(ArrayList<int[]> totalInput, int sampleCount) {
		// 取样
		double times = (double) totalInput.size() / (double) sampleCount;
		int[][] input = new int[sampleCount][3];
		for (int i = 0; i < sampleCount; i++) {
			input[i] = totalInput.get((int) (i * times)).clone();
		}
		return input;
	}

	static public boolean compares(int[][] input, int[][] password,
			int similarity) {
		// 遍历密码
		int sampleCount = input.length;
		// 模糊判断 只要满足一个方向即可
		boolean[] flag = new boolean[sampleCount];
		for (int i = 0; i < sampleCount; i++) {
			flag[i] = false;
		}

		for (int i = 0; i < sampleCount; i++) {
			for (int j = 0; j < 3; j++) {
				if (input[i][j] == password[i][j] && password[i][j] != 0) {
					flag[i] = true;
				}
			}
		}

		// 模糊判断 吻合度大于13/20即可
		int flagCount = 0;
		for (int i = 0; i < sampleCount; i++) {
			if (flag[i])
				flagCount++;
		}
		if (flagCount > similarity)
			return true;

		return false;

	}

	// 当前输入的密码与原先的密码进行比较
	static public boolean compareKeys(int[][] input, int[][] password) {
		return compares(input, password, 14);
	}

	// 当前输入的密码与原先的密码进行比较相似度
	static public boolean compareSimilarity(int[][] input, int[][] password) {
		return compares(input, password, 10);
	}
}
