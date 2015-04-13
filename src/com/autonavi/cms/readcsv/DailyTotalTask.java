package com.autonavi.cms.readcsv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.autonavi.cms.mongo.MongoInstance;
import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.TimeUtil;
import com.autonavi.cms.util.Constants.DBField;
import com.autonavi.vo.DaliyTotal;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 统计入库数据，将数据放入缓存，每隔一段时间同步到数据库一次
 * 
 * @author shuai.qi
 * 
 */
public class DailyTotalTask {
	public static HashMap<String, DaliyTotal>	daliyTotalMap	= new HashMap<>();

	public enum DT_TYPE {
		INSERT, UPDATE, NOUPDATE, REMOVE
	}

	/**
	 * 统计信息入库
	 * 
	 */
	public static void process() {
		String adcode = null;
		DaliyTotal dt = null;
		DBObject dbOldObj = null;
		BasicDBObject newOldObj = null;

		if (daliyTotalMap == null || daliyTotalMap.size() == 0) {
			return;
		}

		Iterator<Entry<String, DaliyTotal>> iter = daliyTotalMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, DaliyTotal> entry = iter.next();
			adcode = entry.getKey();
			dt = entry.getValue();
			if (dt == null) {
				continue;
			}

			dbOldObj = MongoInstance.getInstance().queryByTimeAndAdcode(
					dt.time, dt.adcode);

			newOldObj = new BasicDBObject();
			newOldObj.put("date", TimeUtil.getNowTime());
			newOldObj.put("adcode", adcode);

			if (dbOldObj == null) {
				newOldObj.put("time", dt.time);
				newOldObj.put("insert_count", dt.insertCount);
				newOldObj.put("update_count", dt.updateCount);
				newOldObj.put("no_update_count", dt.noUpdateCount);
				newOldObj.put("remove_count", dt.removeCount);
				MongoInstance.getInstance().AddRecord(newOldObj,
						ConfigAccess.getInstance().BASE_COUNT_TABLE);
			} else {
				newOldObj.put(DBField.ID, dbOldObj.get(DBField.ID));
				newOldObj.put("time", dbOldObj.get("time"));
				newOldObj.put("insert_count",
						Long.parseLong(dbOldObj.get("insert_count").toString())
								+ dt.insertCount);
				newOldObj.put("update_count",
						Long.parseLong(dbOldObj.get("update_count").toString())
								+ dt.updateCount);
				newOldObj.put(
						"no_update_count",
						Long.parseLong(dbOldObj.get("no_update_count")
								.toString()) + dt.noUpdateCount);
				newOldObj.put("remove_count",
						Long.parseLong(dbOldObj.get("remove_count").toString())
								+ dt.removeCount);

				MongoInstance.getInstance().saveRecord(newOldObj,
						ConfigAccess.getInstance().BASE_COUNT_TABLE);
			}
		}

		daliyTotalMap.clear();
	}

	/**
	 * 将统计信息放入缓存
	 * 
	 * @param provinceCode
	 * @param type
	 */
	public static void insert(String provinceCode, DT_TYPE type) {
		DaliyTotal dt = null;
		if (DailyTotalTask.daliyTotalMap.get(provinceCode) == null) {
			dt = new DaliyTotal();
			if (type == DT_TYPE.INSERT) {
				dt.insertCount = 1;
			} else if (type == DT_TYPE.UPDATE) {
				dt.updateCount = 1;
			} else if (type == DT_TYPE.NOUPDATE) {
				dt.noUpdateCount = 1;
			} else if (type == DT_TYPE.REMOVE) {
				dt.removeCount = 1;
			}
			dt.adcode = provinceCode;
			dt.time = TimeUtil.getTimeMillisSmallHour();
			dt.date = TimeUtil.getNowTime();

			DailyTotalTask.daliyTotalMap.put(provinceCode, dt);
		} else {
			dt = DailyTotalTask.daliyTotalMap.get(provinceCode);
			if (type == DT_TYPE.INSERT) {
				dt.insertCount++;
			} else if (type == DT_TYPE.UPDATE) {
				dt.updateCount++;
			} else if (type == DT_TYPE.NOUPDATE) {
				dt.noUpdateCount++;
			} else if (type == DT_TYPE.REMOVE) {
				dt.removeCount++;
			}
			dt.adcode = provinceCode;
			dt.time = TimeUtil.getTimeMillisSmallHour();
			dt.date = TimeUtil.getNowTime();
		}
	}

}
