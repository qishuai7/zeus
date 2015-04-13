package com.autonavi.cms.util;


/**
 * 验证各种逻辑参数是否合法
 * 
 * @author shuai.qi
 * 
 */
public class RegularParam {
	
	/**
	 * java -jar *.jar D:/cwRsync/file_product 20140606000000
	 * 
	 * @param args
	 * @return
	 */
	public static boolean checkReadCSVParam(String[] args) {
		boolean paramCheck = true;

		if (args == null || args.length > 2) {
			System.out.println("请输入参数或参数过多");
			paramCheck = false;
		} else {
			for (int i = 0; i < args.length; i++) {
				if (i == 0) {
					String sourceFolder = args[i];
					if (!FileUtil.checkIsDirectory(sourceFolder)) {
						paramCheck = false;
						break;
					}
				} else if (i == 1) {
					String endTime = args[1];
					if (!TimeUtil.checkTime(endTime)) {
						paramCheck = false;
						break;
					}
				}
			}
		}

		return paramCheck;
	}
}
