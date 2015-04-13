package com.autonavi.cms.main;

import java.util.ArrayList;
import java.util.List;

import com.autonavi.cms.mongo.MongoUtil;
import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.LogUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 给所有省份添加version字段、添加索引
 * 
 * @author shuai.qi
 * 
 */
public class MainAddVersionInfo {
	public static final String	LOGTAG	= "[MainAddVersionInfo]";

	public static void main(String[] args) {
		System.out.println(LogUtil.LogTag(LOGTAG) + " Add version start:");
		processAddVersion();
		System.out.println(LogUtil.LogTag(LOGTAG) + "Add version end.");

		System.out.println(LogUtil.LogTag(LOGTAG) + "Add version index start:");
		processAddVersionIndex();
		System.out.println(LogUtil.LogTag(LOGTAG) + "Add version index end.");
		
		checkVersionCount();
		System.out.println(LogUtil.LogTag(LOGTAG) + "check version end.");
	}

	/**
	 * 添加version索引
	 */
	public static void processAddVersionIndex() {
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		MongoUtil mMongoUtil = new MongoUtil("127.0.0.1", 27017, "CMSBASE0814",
				collectionNameList, INDEX_TYPE.BASE);
		mMongoUtil.init();
	}

	/**
	 * 循环遍历所有省份,添加version字段
	 */
	public static void processAddVersion() {
		// 初始化数据库
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		MongoUtil mongoUtil = new MongoUtil("127.0.0.1", 27017, "CMSBASE0814",
				collectionNameList, INDEX_TYPE.NONE);
		mongoUtil.init();

		// 添加字段信息
		long poiCount = 0;
		final BasicDBObject appendData = new BasicDBObject("$push",
				new BasicDBObject(MongoUtil.DB_FIELD_version,
						MongoUtil.DB_FIELD_version_value));

		DBObject oDBObj = null;
		String curPoiid = null;
		List<DBObject> oDBList = null;

		for (String adcode : collectionNameList) {
			System.out.println("正在处理省份：" + adcode);

			// 根据POIID排序遍历更新数据
			oDBList = mongoUtil.QueryAllMeshID("", adcode);

			while (oDBList.size() > 0) {
				for (int j = 0; j < oDBList.size(); j++) {
					oDBObj = oDBList.get(j);
					curPoiid = oDBObj.get("poiid").toString();

					mongoUtil.appendRecord(oDBObj, appendData, adcode);
				}

				// log
				poiCount += oDBList.size();
				if (poiCount % 20000 == 0) {
					System.out.println(LogUtil.LogTag(LOGTAG) + "Adcode = "
							+ adcode + "; poiCount = " + poiCount);
				}

				oDBList.clear();
				oDBList = null;
				oDBList = mongoUtil.QueryAllMeshID(curPoiid, adcode);
			}
		}

	}

	/**
	 * 检验操作是否完成
	 */
	public static void checkVersionCount() {
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		MongoUtil mMongoUtil = new MongoUtil("127.0.0.1", 27017, "CMSBASE0814",
				collectionNameList, INDEX_TYPE.BASE);
		mMongoUtil.init();

		final BasicDBObject query = new BasicDBObject(
				MongoUtil.DB_FIELD_version, MongoUtil.DB_FIELD_version_value);

		for (String adcode : collectionNameList) {
			System.out.println("正在处理省份：" + adcode);
			long allCount = mMongoUtil.GetCountByAdcode(adcode);
			long versionAllCount = mMongoUtil.GetCountByAdcode(adcode, query);
			if (allCount != versionAllCount) {
				System.out.println(LogUtil.LogTag(LOGTAG) + "Adcode = "
						+ adcode + "; allCount = " + allCount
						+ "; versionAllCount = " + versionAllCount);
			}
		}

	}
}
