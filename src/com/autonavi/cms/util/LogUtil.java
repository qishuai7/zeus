package com.autonavi.cms.util;

public class LogUtil {
	public static String LogTag(String className){
		return className + TimeUtil.getNowTime() + " ";
	}
}
