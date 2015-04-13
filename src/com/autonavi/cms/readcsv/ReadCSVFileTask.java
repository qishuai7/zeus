package com.autonavi.cms.readcsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.autonavi.cms.util.Constants;
import com.autonavi.cms.util.FileUtil;
import com.autonavi.cms.util.Logger;
import com.autonavi.cms.util.TimeUtil;

/**
 * 读取本地磁盘CSV文件，检查合法性，放入队列
 * 
 * @author shuai.qi
 * 
 */
public class ReadCSVFileTask implements Runnable {
	private static final String	TAG			= "[ReadCSVFileTask] ";

	private String				mCsvPath	= null;
	private String				mEndTime	= null;
	private boolean				mIsRunning	= false;

	public ReadCSVFileTask(String csvPath, String endTime) {
		this.mCsvPath = csvPath;
		this.mEndTime = endTime;
	}

	@Override
	public void run() {
		mIsRunning = true;
		// 1. 读取并排序CSV文件
		File[] csvFileArray = getCSVFile(mCsvPath, mEndTime);
		// 2. 入库
		readCSV(csvFileArray);
		mIsRunning = false;
	}

	/**
	 * 按时间顺序读取CSV文件
	 * 
	 * @param sourceFolder
	 * @param endTime
	 */
	private File[] getCSVFile(String sourceFolder, String endTime) {
		Logger.i(TAG + "参数：" + sourceFolder + "; " + endTime);

		ArrayList<File> csvList = new ArrayList<File>();
		FileUtil.getFilePath(sourceFolder, csvList, endTime);
		File[] csvFileArray = FileUtil.sortFile(csvList);

		if (csvList.size() > 0) {
			Logger.i(TAG + "开始CSV文件路径：" + csvFileArray[0].getAbsolutePath()
					+ "; list size: " + csvList.size());
			Logger.i(TAG + "开始CSV文件路径："
					+ TimeUtil.getTimeByMillis(csvFileArray[0].lastModified()));
		} else {
			Logger.i(TAG + "CSV文件个数为零!");
			return null;
		}

		return csvFileArray;
	}

	/**
	 * 循环读取CSV,入库
	 * 
	 * @param csvFileArray
	 * @param oDB
	 */
	private void readCSV(File[] csvFileArray) {
		if (csvFileArray == null) {
			return;
		}

		int csvFileCount = 0;
		int allPOICount = 0;
		CSVFileEntry entry = null;
		BufferedReader br = null;

		for (File csv : csvFileArray) {
			try {
				entry = new CSVFileEntry(csv);
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(csv), "UTF-8"));

				String line = br.readLine(); // ignore first header line
				while ((line = br.readLine()) != null) {
					entry.addData(line);
				}
				QueueBlock.csvQueue.put(entry);

			} catch (Exception e) {
				Logger.error(TAG + "读取CSV文件异常！");
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}

			csvFileCount++;
			allPOICount += entry.getDataCount();
			if (csvFileCount % Constants.LOG_READ_LIMIT == 0) {
				Logger.i(TAG + "正在处理CSV数据文件：" + csv.getAbsolutePath());
				Logger.i(TAG + "read csv poi all count：" + csvFileCount + "->"
						+ allPOICount);
			}
		}

		Logger.i(TAG + "poi all count final：" + allPOICount);
	}

	public boolean isRunning() {
		return mIsRunning;
	}

	public void setEndTime(String mEndTime) {
		this.mEndTime = mEndTime;
	}

}
