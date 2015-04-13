package com.autonavi.cms.main;

import java.util.ArrayList;

import com.autonavi.cms.mongo.MongoUtil;
import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.TimeUtil;

/**
 * 查询所有省份总size
 * 
 * @author shuai.qi
 * 
 */
public class MainAllSize {
	public static void main(String[] args) {
		// ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		// MongoUtil mongoUtil = new MongoUtil("10.2.164.12", 27017,
		// "CMSBASE0814",
		// collectionNameList, INDEX_TYPE.NONE);
		// mongoUtil.init();
		// System.out.println(TimeUtil.getNowTime() + ":" +
		// mongoUtil.getAllDBSize());
		// mongoUtil.Close();

		daliyTotal(20140808, 20140904);

	}

	public static void daliyTotal(int minDay, int mzxDay) {
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		MongoUtil mongoUtil = new MongoUtil("10.61.9.12", 27017,
				"CMSBASE0814", collectionNameList, INDEX_TYPE.NONE);
		// MongoUtil mongoUtil = new MongoUtil("127.0.0.1", 27017,
		// "CMSBASE0814",
		// collectionNameList, INDEX_TYPE.NONE);
		mongoUtil.init();

		long collectionCount = 0;
		long incrCount = 0;
		long allCollectionCount = 0;
		long allIncrCount = 0;

		for (String adcode : collectionNameList) {
			collectionCount = mongoUtil.count(adcode);
			if (collectionCount == 0) {
				continue;
			}
			incrCount = mongoUtil.queryByVersion(minDay, mzxDay, adcode);
			System.out.println(adcode + " AllCount = " + collectionCount
					+ ";IncrCount = " + incrCount + "; 百分比 = " + incrCount
					* 100 / collectionCount + "% 。");

			allCollectionCount += collectionCount;
			allIncrCount += incrCount;
		}

		mongoUtil.Close();
		System.out.println("[更新统计结果]" + " AllCount = " + allCollectionCount
				+ ";IncrCount = " + allIncrCount + "; 百分比 = " + allIncrCount
				* 100 / allCollectionCount + "% 。");
	}

}
