package com.autonavi.cms.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import com.autonavi.cms.mongo.MongoUtil;
import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.GeoCodeBilly;
import com.autonavi.cms.util.TimeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 读取原始数据表，提取需要的数据
 * 
 * @author shuai.qi
 * 
 */
public class ReadDataToQueueTask implements Runnable {
	private String							mTaskName	= null;
	private ArrayBlockingQueue<DBObject>	mQueue		= null;

	private MongoUtil						mMongoUtil	= null;

	public ReadDataToQueueTask(String taskName,
			ArrayBlockingQueue<DBObject> queue) {
		this.mTaskName = taskName;
		this.mQueue = queue;
	}

	@Override
	public void run() {
		System.out.println(mTaskName + "初始化数据库...");
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		// 10.2.164.25
		ConfigAccess db = ConfigAccess.getInstance();
		mMongoUtil = new MongoUtil(db.BASE_HOST, db.BASE_PORT, db.BASE_Name,
				collectionNameList, INDEX_TYPE.NONE);
		mMongoUtil.init();
		
		System.out.println(mTaskName + " dbcollectionSize:"
				+ mMongoUtil.getCollectionNameListSize());

		HashMap<String, DBCollection> collectionMap = mMongoUtil
				.getDbCollectionMap();

		int count = 0;
		DBCursor cur = null;
		DBObject oDBObj = null;
		DBObject dbkeys = (DBObject) JSON
				.parse("{\"serialNumber\":\"\",\"base\":\"\"}");

		for (String adcode : collectionNameList) {
			DBCollection dbColl = collectionMap.get(adcode);
			cur = dbColl.find(new BasicDBObject(), dbkeys);

			System.out.println("正在处理省份："+ adcode + ";该省数据总量：" + cur.count());

			while (cur.hasNext()) {
				count++;
				if (count % 50000 == 0) {
					System.out
							.println(TimeUtil.getNowTime() + " 查询总数:" + count);
				}
				oDBObj = cur.next();
				try {
					DBObject jsonData = extractInfo(oDBObj);
					if(jsonData != null){
						mQueue.put(jsonData);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			cur.close();
		}

		mMongoUtil.Close();
		System.out.println(mTaskName + "处理完毕...");
	}
	
	static String tempStr = null;

	/**
	 * 提取可用字符串
	 * @param dbObj
	 * @return
	 */
	public static DBObject extractInfo(DBObject dbObj) {
		if (dbObj == null) {
			return null;
		}
		
		long sN = Integer.parseInt(dbObj.get(WriteToTempTableTask.DB_FIELD_ID_SN).toString());

		DBObject baseKey = (DBObject) dbObj.get("base");

		try {
			// 组装字符
			int code = Integer.parseInt(baseKey.get("code").toString());
			int province_code = code / 10000 * 10000;
			int city_code = code / 100 * 100;

			baseKey.put("serialNumber", sN);
			baseKey.put("province_code", province_code);
			baseKey.put("city_code", city_code);
			// baseKey.put("meshid", dbObj.get("meshid").toString());
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

			baseKey.put(
					"gridcode_new",
					""
							+ GeoCodeBilly.GetGeoCodeLevel15(
									Float.parseFloat(strX),
									Float.parseFloat(strY)) + "");

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
			baseKey.removeField("version");
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
			System.out.println(e.getMessage() + dbObj.toString());
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
