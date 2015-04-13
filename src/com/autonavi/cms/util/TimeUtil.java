package com.autonavi.cms.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	public static void main(String[] args) throws InterruptedException {
		// System.out.println(getCurrentTimeMillis("2014-05-14 06:53:52"));
		// System.out.println(getCurrentTimeMillis("2014-05-14 18:53:52"));
		// System.out.println(System.currentTimeMillis());

		//getSomeDate(0);
		//System.out.println(getSomeDate(-6));
		//System.out.println(checkTime("20140505999999"));
		//System.out.println(System.currentTimeMillis());
		//Thread.sleep(1000);
		//System.out.println(System.currentTimeMillis());
//		System.out.println();
//		Date s = new Date(1402795155000L);
//		System.out.println(getFormatTime(s));
//		System.out.println(getNowMMDD());
//		ConfigAccess db = ConfigAccess.getInstance();
//		System.out.println(db.BASE_HOST);
//		System.out.println(db.BASE_PORT);
//		System.out.println(db.BASE_Name);
//		System.out.println();
//		
//		System.out.println(db.PRODUCT_HOST);
//		System.out.println(db.PRODUCT_PORT);
//		System.out.println(db.PRODUCT_Name);
		System.out.println(getNowYYMMDD());
		System.out.println(getTimeByMillis(1408959318068L));
		System.out.println(getBeforeDayTime(4));
		System.out.println(getNowTimeDouble());
		System.out.println(TimeUtil.getSomeDate(1));
	}
	
	/**
	 * 获取n天前的时间戳
	 * 
	 * @param day
	 * @return
	 */
	public static long getBeforeDayTime(int day){
		return System.currentTimeMillis() - day*24*60*60*1000;
	}
	
	/**
	 * 检查输入参数是否为日期
	 * @param time
	 * @return
	 */
	public static boolean checkTime(String time) {
		boolean isTime = true;
		
		if(time == null) {
			return false;
		}
		
		DateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
		 try {
			format1.setLenient(false);  // 严格执行日期判断
			format1.parse(time);
		} catch (ParseException e) {
			isTime = false;
		}
		 
		 return isTime;
	}
	
	public static String getFormatTime(Date date) {
		if(date == null){
			return null;
		}
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format1.format(date);
	}
 

	public static String getNowTime() {
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format1.format(new Date());
	}
	
	public static String getNowTimeDouble() {
		DateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
		return format1.format(new Date());
	}
	
	public static String getNowMMDD() {
		DateFormat format1 = new SimpleDateFormat("MMdd");
		return format1.format(new Date());
	}
	
	public static String getNowYYMMDD() {
		DateFormat format1 = new SimpleDateFormat("yyyyMMdd");
		return format1.format(new Date());
	}

	public static long getCurrentTimeMillis(String time) {
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return format1.parse(time).getTime();
		} catch (ParseException e) {
			return Long.parseLong(time);
		}
	}
	
	public static String getTimeByMillis(long timeMillis){
		SimpleDateFormat format =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		return format.format(timeMillis);
	}
	
	/**
	 * 获取当天凌晨时间戳
	 * @return
	 */
	public static long getTimeMillisSmallHour(){
		Date date = null;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			date = sdf.parse(getNowYYMMDD());
			
		} catch (ParseException e)
		{
			System.out.println(e.getMessage());
		}
		
		return date.getTime();
	}

	/**
	 * 获取距离当前天数，某一天的日期
	 * 
	 * @param dayLength
	 *  -1 昨天
	 *   1 明天
	 */
	public static String getSomeDate(int dayLength) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, dayLength);
		String yesterday = new SimpleDateFormat("yyyyMMdd").format(cal
				.getTime());
		return yesterday;				
	}

}
