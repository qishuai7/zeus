package com.autonavi.cms.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.FileUtil;
import com.autonavi.cms.util.TimeUtil;
import com.autonavi.cms.util.Constants.DBField;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * mongo处理工具类
 * 
 * @author shuai.qi
 * 
 */
public class MongoUtil {
	public static final String				DB_FIELD_ID_SN			= "serialNumber";
	public static final String				DB_FIELD_poiid			= "poiid";
	public static final String				DB_FIELD_version		= "version";
	public static final int					DB_FIELD_version_value	= 20140808;
	public static final String				DB_FIELD_isvalid		= "is_valid";

	private String							mHost					= null;
	private int								mPort;
	private String							mDbName					= null;
	private INDEX_TYPE						mIndexType;
	private ArrayList<String>				mCollectionNameList		= null;
	private HashMap<String, DBCollection>	mDbCollectionMap		= new HashMap<String, DBCollection>();
	private MongoClient						mMgc					= null;

	public MongoUtil(String host, int port, String dbName, String collection,
			INDEX_TYPE indexType) {
		this.mHost = host;
		this.mPort = port;
		this.mDbName = dbName;
		this.mIndexType = indexType;
		this.mCollectionNameList = new ArrayList<>();
		this.mCollectionNameList.add(collection);
	}

	public MongoUtil(String host, int port, String dbName,
			ArrayList<String> collectionNameList, INDEX_TYPE indexType) {
		this.mHost = host;
		this.mPort = port;
		this.mDbName = dbName;
		this.mCollectionNameList = collectionNameList;
		this.mIndexType = indexType;
	}

	public enum INDEX_TYPE {
		NONE, // 无索引模式
		BASE, // 原始数据模式
		PRODUCT, // 生产数据模式
		TEMP // 临时add表模式
	}
	
	
	/**
	 * 初始化统计数据库
	 */
	public void initCountDB(){
		DB baseCountDb = mMgc.getDB(ConfigAccess.getInstance().BASE_COUNT_NAME);
		if(baseCountDb == null){
			System.out.println(baseCountDb + " database not exist!");
		}
		
		if (!baseCountDb.collectionExists(ConfigAccess.getInstance().BASE_COUNT_TABLE)) {
			System.out.println("[A]: DB " + ConfigAccess.getInstance().BASE_COUNT_TABLE + ""
					+ " not exit!");

			DBObject options = new BasicDBObject();
			options.put("capped", false);

			baseCountDb.createCollection(ConfigAccess.getInstance().BASE_COUNT_TABLE + "", options);
		}
		
		// 设定数据库写数据模式
		baseCountDb.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		DBCollection dBCollSrc = baseCountDb.getCollection(ConfigAccess.getInstance().BASE_COUNT_TABLE);
		
		mDbCollectionMap.put(ConfigAccess.getInstance().BASE_COUNT_TABLE, dBCollSrc);
	}

	@SuppressWarnings("deprecation")
	public void init() {
		DBCollection dBCollSrc = null;
		try {
			mMgc = new MongoClient(mHost, mPort);
			DB db = mMgc.getDB(mDbName);

			// 初始化统计数据库
			//initCountDB();
			
			if (db == null) {
				System.out.println(mDbName + " database not exist!");
			}

			if (mCollectionNameList != null) {
				for (int i = 0; i < mCollectionNameList.size(); i++) {
					String collectionName = mCollectionNameList.get(i);

					// 删除旧数据
					// if (db.collectionExists(collectionName)) {
					// db.getCollection(collectionName).drop();
					// System.out.println("drop collection : "
					// + collectionName);
					// }

					if (!db.collectionExists(collectionName)) {
						System.out.println("[A]: DB " + collectionName + ""
								+ " not exit!");

						DBObject options = new BasicDBObject();
						options.put("capped", false);

						db.createCollection(collectionName + "", options);
					}

					// 设定数据库写数据模式
					db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
					dBCollSrc = db.getCollection(collectionName);

					// 确认建立索引
					if (mIndexType == INDEX_TYPE.BASE) {
						try {
							dBCollSrc.ensureIndex(new BasicDBObject(
									DB_FIELD_poiid, -1), "poiid_index", true);
							dBCollSrc.ensureIndex(new BasicDBObject(
									DB_FIELD_ID_SN, -1), "sn_index", true);
							dBCollSrc.ensureIndex(new BasicDBObject(
									DB_FIELD_version, -1), "version_index",
									false);
							dBCollSrc.ensureIndex(new BasicDBObject(
									DB_FIELD_isvalid, -1), "isvalid_index",
									false);
						} catch (MongoException.DuplicateKey ex) {
							DBObject messageObj = (DBObject) JSON.parse(ex
									.getMessage());
							String errmsg = messageObj.get("errmsg").toString();
							String poiid = errmsg.substring(
									errmsg.indexOf("\"") + 1,
									errmsg.lastIndexOf("\""));
							System.out.println("[dk poiid]" + poiid + i);
							// FileUtil.setErrorLog("D://error.log", poiid);
							// removedkPOIID(dBCollSrc, poiid);

							// i--;
							// continue;
						}

					} else if (mIndexType == INDEX_TYPE.PRODUCT) {
						dBCollSrc.ensureIndex(new BasicDBObject(DB_FIELD_ID_SN,
								-1), "sn_index", true);
						dBCollSrc.ensureIndex(new BasicDBObject("poiid", -1),
								"poiid_index", true);
					} else if (mIndexType == INDEX_TYPE.TEMP) {
						// dBCollSrc.ensureIndex(new
						// BasicDBObject(DB_FIELD_ID_SN,
						// -1), "sn_index", false);
						// System.out.println("索引添加： " + collectionName);
						// dBCollSrc.ensureIndex(new BasicDBObject("base.poiid",
						// -1),
						// "poiid_index", false);

					}

					mDbCollectionMap.put(collectionName, dBCollSrc);
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		System.out.println(mDbCollectionMap.size());
	}

	public void removedkPOIID(DBCollection dBCollSrc, String poiid) {
		DBObject db = null;

		DBObject dbkeys = (DBObject) JSON
				.parse("{\"_id\":\"\",\"serialNumber\":\"\"}");
		DBObject query = new BasicDBObject(DB_FIELD_poiid, poiid);
		DBCursor cur = dBCollSrc.find(query, dbkeys).sort(
				new BasicDBObject("serialNumber", -1));
		boolean isFirst = true;
		while (cur.hasNext()) {
			db = cur.next();
			System.out.println(TimeUtil.getNowTime() + "{" + poiid + "}"
					+ db.toString());
			if (isFirst) {
				isFirst = false;
				continue;
			} else {
				DBObject delquery = new BasicDBObject("_id", new ObjectId(db
						.get("_id").toString()));
				// System.out.println(delquery);
				WriteResult wr = dBCollSrc.remove(delquery);
				System.out.println(TimeUtil.getNowTime() + "[delete num]"
						+ wr.getN());
			}

		}
	}

	// 获取大于输入ID 的所有MeshID
	public ArrayList<DBObject> QueryAllMeshID(String strMin, String provinceCode) {
		DBCollection dbCollection = mDbCollectionMap.get(provinceCode);
		if (dbCollection == null) {
			System.out.println("queryByPoiid(): province code wrong.");
			return null;
		}

		ArrayList<DBObject> oalRes = new ArrayList<DBObject>();
		DBCursor cursor = null;
		BasicDBObject query = null;
		DBObject strTemp = null;

		DBObject dbkeys = (DBObject) JSON
				.parse("{\"_id\":\"\",\"serialNumber\":\"\",\"poiid\":\"\"}");

		if (null == strMin || strMin.length() < 1) {
			cursor = dbCollection.find()
					.sort(new BasicDBObject(DB_FIELD_poiid, -1)).limit(5000);
		} else {
			query = new BasicDBObject(DB_FIELD_poiid, new BasicDBObject("$lt",
					strMin));
			cursor = dbCollection.find(query, dbkeys)
					.sort(new BasicDBObject(DB_FIELD_poiid, -1)).limit(5000);
		}

		while (cursor.hasNext()) {
			strTemp = (DBObject) cursor.next();

			oalRes.add(strTemp);
		}

		cursor.close();
		return oalRes;
	}

	public void Close() {
		if (mMgc != null) {
			mMgc.close();
		}
	}

	public void AddRecord(DBObject oIn, String province_code) {
		if (mDbCollectionMap != null
				&& mDbCollectionMap.get(province_code) != null) {
			mDbCollectionMap.get(province_code).insert(oIn);
		} else {
			System.out.println("数据集合不存在，插入失败" + province_code);
		}
	}

	public void RemoveRecord(DBObject oIn, String province_code) {
		if (mDbCollectionMap != null
				&& mDbCollectionMap.get(province_code) != null) {
			mDbCollectionMap.get(province_code).remove(oIn);
		} else {
			System.out.println("数据集合不存在，插入失败" + province_code);
		}
	}

	public void AddBlockRecord(List<DBObject> oIn, String province_code) {
		if (mDbCollectionMap != null
				&& mDbCollectionMap.get(province_code) != null) {
			mDbCollectionMap.get(province_code).insert(oIn);
		} else {
			System.out.println("数据集合不存在，插入失败" + province_code);
		}
	}

	public void RemoveOneRecordBySN(int sn, String province_code) {
		BasicDBObject query = null;
		query = new BasicDBObject(DB_FIELD_ID_SN, sn);
		mDbCollectionMap.get(province_code).remove(query);
	}

	public DBObject QueryOneRecordByPoiid(String poiid, String province_code) {
		DBObject oalRes = null;
		BasicDBObject query = null;

		query = new BasicDBObject("base." + DB_FIELD_poiid, poiid);
		DBObject keys = (DBObject) JSON.parse("{\"serialNumber\":\"\"}");

		oalRes = mDbCollectionMap.get(province_code).findOne(query, keys);

		return oalRes;
	}

	public int getCollectionNameListSize() {
		return mDbCollectionMap.size();
	}

	/**
	 * 根据POIID查询数据
	 * 
	 * @param poiid
	 * @param province_code
	 * @return
	 */
	public DBObject queryByPoiid(String poiid, String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("queryByPoiid(): province code wrong.");
			return null;
		}
		DBObject query = new BasicDBObject(DB_FIELD_poiid, poiid);
		DBObject keys = (DBObject) JSON
				.parse("{\"serialNumber\":\"\",\"x\":\"\",\"y\":\"\",\"address\":\"\",\"telephone\":\"\",\"name\":\"\",\"is_valid\":\"\"}");

		// DBObject keys = new BasicDBObject(DBField.SERIAL_NUMBER, 1);
		return dbCollection.findOne(query, keys);
	}
	
	/**
	 * 根据版本信息查询数量
	 * 
	 * @param minVersion
	 * @param maxVersion
	 * @param province_code
	 * @return
	 */
	public long queryByVersion(int minVersion,int maxVersion, String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("queryByVersion(): province code wrong.");
			return 0;
		}
		
		BasicDBObject query = new BasicDBObject();
		query.append(DB_FIELD_version, new BasicDBObject("$gt",minVersion).append("$lt",maxVersion));
		query.append(DB_FIELD_isvalid, 0);

		return dbCollection.count(query);
	}
	
	
	/**
	 * 查询集合大小
	 * 
	 * @param province_code
	 * @return
	 */
	public long count(String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("queryByVersion(): province code wrong.");
			return 0;
		}
		
		return dbCollection.count();
	}
	
	
	
	/**
	 * 根据时间戳获取某天统计信息
	 * @param time  某天凌晨时间戳
	 * @return
	 */
	public DBObject queryByTimeAndAdcode(long time,String adcode) {
		DBCollection dbCollection = mDbCollectionMap.get(ConfigAccess.getInstance().BASE_COUNT_TABLE);
		if (dbCollection == null) {
			System.out.println("queryByTime(): province code wrong." + ConfigAccess.getInstance().BASE_COUNT_TABLE);
			return null;
		}
		DBObject query = new BasicDBObject("time", time);
		query.put("adcode", adcode);
		return dbCollection.findOne(query);
	}

	public void saveRecord(DBObject obj, String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("saveRecord(): province code wrong.");
		}
		WriteResult wr = dbCollection.save(obj);
		if (wr.getN() == 0) {
			System.out.println("save error");
		}
	}

	public void updateRecord(DBObject obj, DBObject query, String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("saveRecord(): province code wrong.");
		}
		dbCollection.update(query, obj);
	}

	public void appendRecord(DBObject oldObj, DBObject obj, String province_code) {
		DBCollection dbCollection = mDbCollectionMap.get(province_code);
		if (dbCollection == null) {
			System.out.println("saveRecord(): province code wrong.");
		}
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(oldObj.get(
				"_id").toString()));

		dbCollection.update(query, obj);
	}

	public long GetCountByAdcode(String adcode) {
		DBCollection dbCollection = mDbCollectionMap.get(adcode);
		if (dbCollection == null) {
			System.out.println("GetCount(): province code wrong.");
		}
		return dbCollection.getCount();
	}

	public long GetCountByAdcode(String adcode, DBObject query) {
		DBCollection dbCollection = mDbCollectionMap.get(adcode);
		if (dbCollection == null) {
			System.out.println("GetCount(): province code wrong.");
		}
		return dbCollection.getCount(query);
	}

	/**
	 * 获取数据库size
	 * 
	 * @return
	 */
	public long getAllDBSize() {
		long dbSize = 0;
		Iterator iter = mDbCollectionMap.entrySet().iterator();
		while (iter.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
			DBCollection val = (DBCollection) entry.getValue();
			dbSize += val.getCount();
		}
		return dbSize;
	}

	public HashMap<String, DBCollection> getDbCollectionMap() {
		return mDbCollectionMap;
	}

}
