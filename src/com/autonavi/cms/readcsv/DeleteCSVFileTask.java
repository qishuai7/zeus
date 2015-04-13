package com.autonavi.cms.readcsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.autonavi.cms.util.FileUtil;
import com.autonavi.cms.util.Logger;
import com.autonavi.cms.util.TimeUtil;

/**
 * 删除已处理过的数据 1. 保证数据实时入库 2. 定期删除符合规则的数据
 * 
 * @author shuai.qi
 * 
 */
public class DeleteCSVFileTask {
	public static final String	TAG				= "[DeleteCSVFileTask]";
	public static final String	DeleteFilePath	= "D:\\CmsDeleteInfo.log";
	public static final int		EndDay			= 4;						// 删除n天前的数据

	/**
	 * 保存已经处理过得文件路径
	 * 
	 * @param csvFile
	 */
	public static void addFile(File csvFile) {
		if (csvFile == null || !csvFile.exists()) {
			Logger.i(TAG + "file no exists .");
			csvFile.mkdir();
			Logger.i(TAG + "create file." + DeleteFilePath);
			return;
		}

		FileUtil.writeToFile(DeleteFilePath, csvFile.getAbsolutePath());
	}

	/**
	 * 循环删除过期文件
	 * 
	 */
	public static void deleteFile() {
		File csvLogFile = new File(DeleteFilePath);
		if (csvLogFile == null || !csvLogFile.exists()) {
			Logger.i(TAG + "file no exists .");
			return;
		}

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					csvLogFile), "UTF-8"));
			File csvFile = null;
			long csvFileTime = 0;
			long csvFileEndTime = TimeUtil.getBeforeDayTime(EndDay);

			String data = br.readLine();
			while (data != null) {
				csvFile = new File(data);
				if (csvFile.exists()) {
					csvFileTime = csvFile.lastModified();

					if (csvFileTime < csvFileEndTime) {
						csvFile.delete();
					}
				}

				data = br.readLine();
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
