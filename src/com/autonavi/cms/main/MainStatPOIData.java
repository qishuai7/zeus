package com.autonavi.cms.main;

import java.io.File;
import java.util.ArrayList;

import com.autonavi.cms.util.CityCode;

/**
 * 统计厦门POI数据大小
 * 
 * @author shuai.qi
 * 
 */
public class MainStatPOIData {
	public static void main(String[] args) {
		String filePath = "D:/data140613/data140613city/dbdata/";
		// printAllCitySize(filePath);
		printAllProvinceSize(filePath);
	}

	public static void printAllCitySize(String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] dirFile = file.listFiles();
			for (File f : dirFile) {
				if (f.isDirectory()) {
					final long totalByte = getTotalSizeOfFilesInDir(f);
					float totleSize = Get2Num(totalByte);
					System.out.println(f.getName().substring(
							f.getName().length() - 4)
							+ " : " + totleSize + " MB");
				}
			}
		}

	}

	public static float Get2Num(float num) {
		float t = num / 1024 / 1024;
		if (t == 0)
			t = (float) 0.01;
		float b = (float) (Math.round(t * 100)) / 100;
		return b;
	}

	// 递归方式 计算文件的大小
	public static long getTotalSizeOfFilesInDir(File file) {
		if (file.isFile())
			return file.length();
		final File[] children = file.listFiles();
		long total = 0;
		if (children != null)
			for (final File child : children)
				total += getTotalSizeOfFilesInDir(child);
		return total;
	}

	public static void printAllProvinceSize(String path) {
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		for (int i = 0; i < collectionNameList.size(); i++) {
			String provinceCode = collectionNameList.get(i).substring(0, 2);

			long sumSize = 0;
			File file = new File(path);
			if (file.isDirectory()) {
				File[] dirFile = file.listFiles();
				for (File f : dirFile) {
					String cityCode = f.getName().substring(
							f.getName().length() - 4, f.getName().length() - 2);
					if (provinceCode.equals(cityCode)) {
						if (f.isDirectory()) {
							final long totalByte = getTotalSizeOfFilesInDir(f);
							sumSize += totalByte;
						}
					}

				}
			}
			
			//System.out.println(provinceCode + " : " + Get2Num(sumSize));
			//System.out.println(provinceCode);
			System.out.println(Get2Num(sumSize) + " MB");
		}

	}
}
