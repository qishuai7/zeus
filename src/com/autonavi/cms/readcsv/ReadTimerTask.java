package com.autonavi.cms.readcsv;

import java.util.TimerTask;

import com.autonavi.cms.util.Logger;
import com.autonavi.cms.util.TimeUtil;

/**
 * 如果定时检查线程执行完毕，重启线程
 * 
 * @author shuai.qi
 * 
 */
public class ReadTimerTask extends TimerTask {
	public static final String	TAG				= "[ReadTimerTask]";
	private ReadCSVFileTask		mReadCSVTask	= null;

	public ReadTimerTask(String csvPath, String endTime) {
		mReadCSVTask = new ReadCSVFileTask(csvPath, endTime);
	}

	@Override
	public void run() {
		Logger.i(TAG + "定时检测read thread是否完成" + mReadCSVTask.isRunning());
		// 同步日志至数据库
		// DailyTotalTask.process();
		// 删除已处理文件
		// DeleteCSVFileTask.deleteFile();

		if (!mReadCSVTask.isRunning()) {
			Logger.i(TAG + "重启read thread. end time = " + TimeUtil.getSomeDate(-4) + "000000");
			// 重启线程
			mReadCSVTask.setEndTime(TimeUtil.getSomeDate(-4) + "000000");
			new Thread(mReadCSVTask).start();
		}
	}
}
