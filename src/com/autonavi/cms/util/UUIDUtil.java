package com.autonavi.cms.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 通用唯一识别码
 * 32位UUID +  14位日期 + 4位随机数 + 3位 平台类型 (淘金android首字母)
 * @author shuai.qi 4aea8471a713490d92391aa52ea86c55201410171624525661ta
 */
public class UUIDUtil {
	public static String getUUID() {
		StringBuffer sb = new StringBuffer(UUID.randomUUID().toString()
				.replace("-", ""));
		sb.append(getNowTime());
		sb.append((int) (Math.random() * 10000));
		sb.append("ta");

		return sb.toString();
	}

	public static String getNowTime() {
		DateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
		return format1.format(new Date());
	}
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("^[a-zA-Z]*");
		Matcher matcher1 = pattern.matcher("ab1e");
		Matcher matcher2 = pattern.matcher("1bcde");
		boolean s1 = matcher1.matches();
		boolean s2 = matcher2.matches();
		  String str = "abcd12345efghijklmn";
	        //检查str中间是否包含12345
	       // System.out.println(str + ":" + str.matches("\\w+12345\\w+")); //true
	        System.out.println("a".matches("^[a-zA-Z]")); //true
	        
	        
	       	Pattern pattern1 = Pattern.compile("^[a-zA-Z]");
	    	System.out.println(pattern1.matcher("11a").find());
		//System.out.println( "s1: =" + s1 + ";s2 = " + s2);
	}

}
