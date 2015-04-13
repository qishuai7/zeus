package com.autonavi.cms.convert;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.autonavi.cms.mongo.MongoUtil;
import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.TimeUtil;
import com.mongodb.DBObject;
import com.mongodb.util.JSONParseException;

public class WriteToTempTableTask implements Runnable {
	public static String					DB_FIELD_ID_SN	= "serialNumber";
	public static String					DB_FIELD_ID_VALID	= "is_valid";
	

	private String							mTaskName		= null;
	private ArrayBlockingQueue<DBObject>	mQueue			= null;
	private MongoUtil						mMongoUtil		= null;


	public WriteToTempTableTask(String taskName,
			ArrayBlockingQueue<DBObject> queue) {
		this.mTaskName = taskName;
		this.mQueue = queue;
	}

	@Override
	public void run() {
		System.out.println(mTaskName + "初始化数据库...");
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		// 10.5.103.41
		ConfigAccess db = ConfigAccess.getInstance();
		mMongoUtil = new MongoUtil(db.PRODUCT_HOST, db.PRODUCT_PORT, db.PRODUCT_Name,
				collectionNameList, INDEX_TYPE.PRODUCT);
		mMongoUtil.init();
		System.out.println(mTaskName + "dbcollectionSize:"
				+ mMongoUtil.getCollectionNameListSize());

		int allPOICount = 0;
		long allQuerytime = 0;
		DBObject oObj = null;

		while (true) {
			allPOICount++;

			try {
				oObj = mQueue.take();
				if(oObj == null){
					continue;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			// 提取数据、入库;取某个省份的poi
			String province_code = oObj.get("province_code").toString();
			if (province_code == null || province_code.length() == 0) {
				System.out.println("无省份字段" + oObj);
				continue;
			}

			long starttime = System.currentTimeMillis();
			try {
				mMongoUtil.AddRecord(oObj, province_code);

			} catch (JSONParseException e) {
				System.out.println("JSONParseException出错：" + oObj);
				System.out.println("JSONParseException出错：" + e.getMessage()
						+ "\n");
				continue;
			} catch (Exception e) {
				//System.out.println("Exception：" + oObj);
				System.out.println("Exception：" + e.getMessage() + "\n");
				continue;
			}

			allQuerytime += (System.currentTimeMillis() - starttime);
			if (allPOICount % 50000 == 0) {
				System.out.println(mTaskName + "\t" + TimeUtil.getNowTime()
						+ " All size = " + allPOICount + ";入库平均时间(毫秒)："
						+ allQuerytime / allPOICount);
			}

		}

	}

}
