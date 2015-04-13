package com.autonavi.cms.readcsv;

import com.autonavi.cms.util.Constants;
import com.autonavi.cms.util.TimeUtil;
import com.autonavi.cms.util.Constants.CSVField;
import com.autonavi.cms.util.Constants.DBField;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

public class CSVEntry {
    private static final String TAG = "[CSVEntry] ";

    private String mPoiid;
    
    private int mIsValid;

    private DBObject mDBObject;

    private long mSerialNumber;

    private String mProvinceCode;

    private CSVEntry() {
    }

    public String getPoiid() {
        return mPoiid;
    }
    
    public int getIsValid() {
        return mIsValid;
    }

    public DBObject getDBObject() {
        return mDBObject;
    }

    public long getSerialNumber() {
        return mSerialNumber;
    }

    public String getProvinceCode() {
        return mProvinceCode;
    }

    @Override
    public String toString() {
        return mDBObject.toString();
    }

    public static CSVEntry parse(String line) {
        if (line == null) {
            return null;
        }
        String[] array = line.split(Constants.SEPARATOR_TAB);
        if (array == null || array.length < 3) {
            System.out.println("[Warning] wrong cvs data：" + line);
            return null;
        }
        if (array.length != 5) {
            System.out.println(TAG + "-------> wrong line: " + line);
        }

        CSVEntry entry = new CSVEntry();
        entry.mPoiid = array[0];
        long sn = Long.parseLong(array[2]);
        entry.mSerialNumber = sn;
        try {
        	DBObject  dbObj = (DBObject) JSON.parse(array[1]);
        	int isValid = Integer.parseInt(dbObj.get(CSVField.IS_VALID).toString());
            entry.mIsValid = isValid;
        	entry.mDBObject = extractInfo(dbObj,sn,isValid);
        	if(entry.mDBObject == null){
        		return null;
        	}
        } catch (JSONParseException e) {
            System.out.println(TAG + "JSONParseException: " + e.getMessage());
            return null;
        }
        
        DBObject obj = entry.mDBObject;
        obj.put(DBField.SERIAL_NUMBER, entry.mSerialNumber);

        String provinceCode = getProvinceCode(obj);
        if (provinceCode == null) {
            return null;
        }
        entry.mProvinceCode = provinceCode;

        return entry;
    }

    private static String getProvinceCode (DBObject obj) {
        String provinceCode = obj.get(CSVField.PROVINCE_CODE).toString();
		if (provinceCode == null || provinceCode.length() != 6) {
            return null;
        }
        return provinceCode;
    }
    

	/**
	 * 提取可用字符串
	 * @param dbObj
	 * @return
	 */
	public static DBObject extractInfo(DBObject dbObj,long sn ,int isValid) {
		if (dbObj == null) {
			return null;
		}
		if(sn <= 0){
			System.out.println("[sn非法]" + dbObj);
			return null;
		}
		
		DBObject baseKey = (DBObject) dbObj.get("base");
		
		try {
			// 删除非法数据
			//if(isValid != 0){
			//	return null;
			//}
			if(baseKey.get("code") == null || baseKey.get("code").toString().length() == 0){
				return null;
			}
			
			// 组装字符
			int code = Integer.parseInt(baseKey.get("code").toString());
			int province_code = code / 10000 * 10000;
			int city_code = code / 100 * 100;

			baseKey.put("serialNumber", sn);
			baseKey.put("province_code", province_code);
			baseKey.put("city_code", city_code);
			baseKey.put(CSVField.IS_VALID, isValid);
			baseKey.put("version", Integer.parseInt(TimeUtil.getNowYYMMDD()));
			if(	dbObj.get("update_time") != null){
				String updateTime = dbObj.get("update_time").toString();
				baseKey.put("update_time_new", TimeUtil.getCurrentTimeMillis(updateTime));
			}else{
				baseKey.put("update_time_new", System.currentTimeMillis());
			}
			
			// 如果是非法字符，简单处理
			if(isValid != 0){
				return baseKey;
			}
			
			if (baseKey.get("name") == null) {
				return null;
			}
			// 检测json是否合法
			String strX = baseKey.get("x").toString();
			String strY = baseKey.get("y").toString();
			double x = Double.valueOf(strX);
			double y = Double.valueOf(strY);
			if (!checkLocInChina(y, x)) {
				return null;
			}

			if (baseKey.containsKey("tag")) {
				baseKey.removeField("tag");
			}
			if (baseKey.containsKey("from_field")) {
				baseKey.removeField("from_field");
			}

			baseKey.removeField("group");
			baseKey.removeField("container");
			baseKey.removeField("matchprecision");
			baseKey.removeField("buscode");
			baseKey.removeField("querylevel");
			baseKey.removeField("imageid");
			baseKey.removeField("url");
			baseKey.removeField("keywords");
			baseKey.removeField("introduction");
			baseKey.removeField("xml");
			baseKey.removeField("cpid");
			baseKey.removeField("linkid");
			baseKey.removeField("modify_time");
			baseKey.removeField("checked");
			baseKey.removeField("reserved");
			baseKey.removeField("global_id");
			baseKey.removeField("mesh_poiid");
			baseKey.removeField("mesh");
			baseKey.removeField("alias");
			baseKey.removeField("addr_other");
			baseKey.removeField("addr_reg");
			baseKey.removeField("streetno");
			baseKey.removeField("road_enjion");
			baseKey.removeField("distance");
			baseKey.removeField("type_info");

			baseKey.removeField("admin");
			baseKey.removeField("vanity_admin");
			baseKey.removeField("web_pop");
			baseKey.removeField("biz_zone");
			baseKey.removeField("brand_code");
			baseKey.removeField("brand_name");
			baseKey.removeField("brand_icon");
			baseKey.removeField("importance");
			baseKey.removeField("email");
			baseKey.removeField("portal_url");
			baseKey.removeField("fax");
			baseKey.removeField("picture");
			baseKey.removeField("time_reg");
			baseKey.removeField("time_text");
			baseKey.removeField("parent_rel");
			baseKey.removeField("bg_rel");
			baseKey.removeField("data_info");
			baseKey.removeField("addr_split");
			baseKey.removeField("multi_src_type");
			baseKey.removeField("main_name");
			baseKey.removeField("sub_name");
			baseKey.removeField("bcs");
			baseKey.removeField("pixelx");
			baseKey.removeField("pixely");
			baseKey.removeField("src_id");

		} catch (Exception e) {
			System.out.println("[json转换出错]" + e.getMessage() + dbObj.toString());
			e.printStackTrace();
			return null;
		}

		return baseKey;
	}

	// 经纬度
	private final static double East = 145.0D;
	private final static double South = 1.0D;
	private final static double West = 50.0D;
	private final static double North = 65.0D;

	/**
	 * 检查Location是否在 1<lat<65 50<lon<145 除GPS外，其他NETWORK、APS、CNet都需要判断经纬度是否有效
	 * 
	 * @param loc
	 * @return
	 */
	public static boolean checkLocInChina(double y, double x) {
		if ((y > South && y < North) && (x > West && x < East))
			return true;
		else
			return false;
	}
}
