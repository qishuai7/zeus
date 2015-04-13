package com.autonavi.cms.main;

import java.util.Timer;

import com.autonavi.cms.readcsv.ReadTimerTask;
import com.autonavi.cms.readcsv.WriteCSVFileTask;
import com.autonavi.cms.util.Logger;
import com.autonavi.cms.util.RegularParam;
import com.autonavi.cms.util.TimeUtil;

/**
 * Main CMS数据入库
 * 
 * @author shuai.qi
 * @date 20148014
 */
public class MainProcessCSVFile {
	/**
	 * java -jar *.jar F:/cwRsync/file_product 20140802000000(可省略)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String[] parms = checkParams(args);
		if (parms == null) {
			return;
		}

		String csvPath = parms[0]; 
		String endTime = parms[1]; // "20140928000000";

		Logger.i("Main read CSV files start:" + TimeUtil.getNowTime());

		startReadThread(csvPath, endTime);
		startWriteThread();

	}

	/**
	 * 读取本地数据
	 * 
	 * @param queue
	 * @param csvPath
	 *            路径
	 * @param endTime
	 *            最大文件日期
	 */
	public static void startReadThread(String csvPath, String endTime) {
		ReadTimerTask readTimerTask = new ReadTimerTask(csvPath, endTime);
		Timer readTimer = new Timer();
		readTimer.schedule(readTimerTask, 0, 60 * 60 * 1000);
	}

	/**
	 * 将队列文件入库
	 * 
	 * @param queue
	 */
	public static void startWriteThread() {
		new Thread(new WriteCSVFileTask()).start();
	}

	/**
	 * 输入参数校验
	 * 
	 * @param args
	 * @return
	 */
	public static String[] checkParams(String[] args) {
		if (!RegularParam.checkReadCSVParam(args)) {
			Logger.i("input parms error，try again!");
			return null;
		}

		String csvPath = null;
		String endTime = null;

		if (args.length == 2) {
			csvPath = args[0];
			endTime = args[1];
		} else if (args.length == 1) {
			csvPath = args[0];
			endTime = TimeUtil.getSomeDate(-4) + "000000";
		} else {
			// default params "D://cwRsync//file_product" 
			csvPath = "D://cwRsync//test";
			endTime = TimeUtil.getSomeDate(-4) + "000000";
		}

		String[] params = { csvPath, endTime };

		return params;
	}

}
