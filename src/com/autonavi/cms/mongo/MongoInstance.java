package com.autonavi.cms.mongo;

import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.ConfigAccess;

/**
 * 初始化数据库,单例
 * 
 * @author shuai.qi
 * 
 */
public class MongoInstance {
	private static MongoUtil	mInstance	= null;

	private MongoInstance() {

	}

	public static synchronized MongoUtil getInstance() {
		if (mInstance == null) {
			init();
		}

		return mInstance;
	}

	private static void init() {
		int mode = ConfigAccess.getInstance().MODE;

		System.out.println("[初始化数据库]" + ConfigAccess.getInstance().BASE_HOST + ConfigAccess.getInstance().BASE_Name);

		if (mode == 0) { // 生产模式，有索引
			mInstance = new MongoUtil(ConfigAccess.getInstance().BASE_HOST,
					ConfigAccess.getInstance().BASE_PORT,
					ConfigAccess.getInstance().BASE_Name,
					CityCode.GetAllProvinceCode(), INDEX_TYPE.BASE);
		}

		mInstance.init();

		System.out.println("[数据库初始化完成] dbcollectionSize:"
				+ mInstance.getCollectionNameListSize());
	}

}
