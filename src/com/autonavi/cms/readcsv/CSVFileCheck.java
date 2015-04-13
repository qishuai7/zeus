package com.autonavi.cms.readcsv;

import java.io.UnsupportedEncodingException;

import com.autonavi.cms.util.TimeUtil;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 
 * 处理csv文件
 * 
 * @author shuai.qi
 * 
 */
public class CSVFileCheck {

	/**
	 * 读取CSV文件一条记录
	 * @param line
	 * @return
	 */
	public static DBObject processCsv(String line) {
//		String tempStr = null;

		String[] st = line.split("	");
		if (st == null) {
			return null;
		}
		if (st.length < 3) {
			return null;
		}
		
		String data = st[1];
		int serialNumber = Integer.parseInt(st[2]);

		DBObject oObj = (DBObject) JSON.parse(data);
		// 验证数据是否有效
		String isValid = oObj.get("is_valid").toString();
		if (!isValid.equals("0")) {
			return null;
		}
		oObj.put("serialNumber", serialNumber);
		if(isValid.equals("0")){
			int cityCode = Integer.parseInt(((DBObject)oObj.get("base")).get("code").toString());
			int province_code = cityCode / 10000 * 10000;
			oObj.put("city_code", cityCode);
			oObj.put("province_code", province_code);
			
			if(	oObj.get("update_time") != null){
				String updateTime = oObj.get("update_time").toString();
				oObj.put("update_time_new", TimeUtil.getCurrentTimeMillis(updateTime));
			}else{
				oObj.put("update_time_new", System.currentTimeMillis());
			}
		}
		
		return oObj;
	}
	
	
	/**
	 * 检测数据是否合法
	 *  现在只检查  拼音、英文名称不能包含中文
	 *  
	 * @param dbobject
	 * @return
	 */
	public static boolean checkData(DBObject oDBObj){
		if(oDBObj.get("multi_name") != null && ((DBObject) oDBObj.get("multi_name")).get("py") != null){
			String py = ((DBObject) oDBObj.get("multi_name")).get("py").toString();
			if(ifContainChineseChar(py) || ifBadChar(py)){
				return false;
			}
		}
		
		if(oDBObj.get("multi_name") != null && ((DBObject) oDBObj.get("multi_name")).get("eng") != null){
			String eng = ((DBObject) oDBObj.get("multi_name")).get("eng").toString();
			if(ifContainChineseChar(eng) || ifBadChar(eng)){
				return false;
			}
		}
		
		if(oDBObj.get("multi_addr") != null && ((DBObject) oDBObj.get("multi_addr")).get("py") != null){
			String py = ((DBObject) oDBObj.get("multi_addr")).get("py").toString();
			if(ifContainChineseChar(py) || ifBadChar(py)){
				return false;
			}
		}
		if(oDBObj.get("multi_addr") != null && ((DBObject) oDBObj.get("multi_addr")).get("eng") != null){
			String eng = ((DBObject) oDBObj.get("multi_addr")).get("eng").toString();
			if(ifContainChineseChar(eng) || ifBadChar(eng)){
				return false;
			}
		}
		
		
		return true;
	}
	
	// 是否乱码
	public static boolean ifBadChar(String str){
		if(str != null){
			String checkStr;
			try {
				checkStr = new String(str.getBytes(),"utf-8");
				if(checkStr.contains("??")){
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	// 是否包含中文
	public static boolean ifContainChineseChar(String str){
		boolean ret = false;
		if(str != null){
			for (int i = 0; i < str.length(); i++) {
				String c = str.substring(i,i+1);
				if(java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", c)){
					ret = true;
					break;
				}
			}
		}
		
		return ret;
	}
		
}
