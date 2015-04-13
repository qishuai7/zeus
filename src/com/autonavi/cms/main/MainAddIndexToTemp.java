package com.autonavi.cms.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.autonavi.cms.mongo.MongoInstance;
import com.autonavi.cms.mongo.MongoUtil;
import com.autonavi.cms.mongo.MongoUtil.INDEX_TYPE;
import com.autonavi.cms.util.CityCode;
import com.autonavi.cms.util.TimeUtil;
import com.autonavi.cms.util.Constants.DBField;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 查询所有省份总size
 * 
 * @author shuai.qi
 *
 */
public class MainAddIndexToTemp {
	public static void main(String[] args) {
	//	System.out.println("Main 添加索引");
//		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
//		MongoUtil mMongoUtil = new MongoUtil("10.2.164.12", 27018, "CMSTEMP0811",
//				collectionNameList, INDEX_TYPE.TEMP);
//		mMongoUtil.init();
//		System.out.println(" 索引创建完毕；开始去重复poiid：");
		
		System.out.println("去重复开始");
		processRepetitionPOIID();
	}
	
	public static void processRepetitionPOIID(){
		ArrayList<String> collectionNameList = CityCode.GetAllProvinceCode();
		MongoUtil mMongoUtil = new MongoUtil("10.2.164.12", 27017, "CMSBASE0814",
				collectionNameList, INDEX_TYPE.NONE);
//		MongoUtil mMongoUtil = new MongoUtil("127.0.0.1", 27017, "CMSTest",
//		collectionNameList, INDEX_TYPE.TEMP);
		mMongoUtil.init();
		
		for (String adcode : collectionNameList) {
			if(adcode.equals("110000") || adcode.equals("120000")){
				continue;
			}
			int count = 0;
			int deleteCount = 0;
			DBObject oDBObj = null;
			DBObject oPreDBObj = null;
			
			//String adcode = "640000";
			System.out.println("正在处理省份："+ adcode );
			List<DBObject> oDBList = mMongoUtil.QueryAllMeshID("", adcode);
			
			while(oDBList.size() > 0){
				String curPoiid = null;
				
				for (int j = 0; j < oDBList.size(); j++) {
					count++;
					oDBObj = oDBList.get(j);
					curPoiid = oDBObj.get("poiid").toString();
					
					if(oPreDBObj == null){
						oPreDBObj = oDBObj;
					}else{
						if(oPreDBObj.get("poiid").toString().equals(curPoiid)){
							int oSn = Integer.parseInt(oDBObj.get("serialNumber").toString());
							int oPreSn = Integer.parseInt(oPreDBObj.get("serialNumber").toString());
							if(oPreSn >= oSn){
								mMongoUtil.RemoveRecord(new BasicDBObject("_id", new ObjectId(oDBObj.get("_id").toString())), adcode);
							}else{
								mMongoUtil.RemoveRecord(new BasicDBObject("_id", new ObjectId(oPreDBObj.get("_id").toString())), adcode);
							}
							deleteCount++;
						}else{
							oPreDBObj = oDBObj;
						}
					}
					
					if(count % 5000 == 0){
						System.out.println(TimeUtil.getNowTime() + "总数据个数:" + count + ";删除个数：" + deleteCount);
					}
				}
				
				oDBList.clear();
				oDBList = null;
		        // 集合总个数30403484/5000=6080
				oDBList = mMongoUtil.QueryAllMeshID(curPoiid,adcode);
			}
			

		
		}
		
		System.out.println("去重复结束");
	}
}
