package com.data;

import java.util.ArrayList;

public class MyAlgorithm {

	static public int[][] sampling(ArrayList<int[]> totalInput, int sampleCount) {
		// ȡ��
		double times = (double) totalInput.size() / (double) sampleCount;
		int[][] input = new int[sampleCount][3];
		for (int i = 0; i < sampleCount; i++) {
			input[i] = totalInput.get((int) (i * times)).clone();
		}
		return input;
	}

	static public boolean compares(int[][] input, int[][] password,
			int similarity) {
		// ��������
		int sampleCount = input.length;
		// ģ���ж� ֻҪ����һ�����򼴿�
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

		// ģ���ж� �Ǻ϶ȴ���13/20����
		int flagCount = 0;
		for (int i = 0; i < sampleCount; i++) {
			if (flag[i])
				flagCount++;
		}
		if (flagCount > similarity)
			return true;

		return false;

	}

	// ��ǰ�����������ԭ�ȵ�������бȽ�
	static public boolean compareKeys(int[][] input, int[][] password) {
		return compares(input, password, 14);
	}

	// ��ǰ�����������ԭ�ȵ�������бȽ����ƶ�
	static public boolean compareSimilarity(int[][] input, int[][] password) {
		return compares(input, password, 10);
	}
}
