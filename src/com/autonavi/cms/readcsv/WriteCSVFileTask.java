package com.autonavi.cms.readcsv;

import com.autonavi.cms.mongo.MongoInstance;
import com.autonavi.cms.readcsv.DailyTotalTask.DT_TYPE;
import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.Constants;
import com.autonavi.cms.util.FileUtil;
import com.autonavi.cms.util.Logger;
import com.autonavi.cms.util.TimeUtil;
import com.autonavi.cms.util.Constants.CSVField;
import com.autonavi.cms.util.Constants.DBField;
import com.mongodb.DBObject;

/**
 * 将队列文件入库
 * 
 * @author shuai.qi
 *
 */
public class WriteCSVFileTask implements Runnable {
    private static final String TAG = "[WriteCSVFileTask] ";
    
    private static int POI_No_Update_Count = 0;
    private static int POI_Update_Count = 0;
    private static int POI_Insert_Count = 0;
    private static int POI_Error_Valid = 0;


    @Override
    public void run() {
        int csvFileCount = 0;
        int allPOICount = 0;
        long allQuerytime = 0;
        CSVFileEntry fileEntry = null;
        CSVEntry entry = null;
        

        long beginTime = System.currentTimeMillis();
        while (true) {
            try {
            	
                fileEntry = QueueBlock.csvQueue.take();
                for (int i = 0; i <  fileEntry.getCSVEntryList().size(); i++ ) {
                	entry = fileEntry.getCSVEntryList().get(i);
                	try {
                		writeToDB(entry);
					}catch (Exception e) {
						Logger.i("[error1 exception] " + entry);
						e.printStackTrace();
						Logger.i(e.getCause() + e.getMessage());
						FileUtil.writeToFile("D://csverror.log", entry.toString());
						continue;
					}
                }
                
            } catch (InterruptedException e) {
            	Logger.i("InterruptedException");
                continue;
            } catch (Exception e) {
            	Logger.i("[error2 exception] " + entry);
            	Logger.i(e.getCause() + e.getMessage());
				continue;
			}

            csvFileCount ++;
            allPOICount += fileEntry.getDataCount();
            if (csvFileCount % Constants.LOG_LIMIT == 0 && allPOICount != 0) {
                long endTime = System.currentTimeMillis();
                allQuerytime += (endTime - beginTime);
                Logger.i(TAG + TimeUtil.getNowTime() + " All size = " + allPOICount
                        + ";入库平均时间(毫秒)：" + allQuerytime / allPOICount + "; queue size: " + QueueBlock.csvQueue.size());
                Logger.i("[数据统计]"+ "All Count =" + allPOICount + ";POI No Update = " + POI_No_Update_Count 
                		+  "; POI Update Count = " + POI_Update_Count + "; POI Insert Count = " + POI_Insert_Count 
                		+"; POI_Error_Valid = " + POI_Error_Valid);
                beginTime = endTime;
            }
            
            fileEntry.deleteFile();
            //DeleteCSVFileTask.addFile(fileEntry.getFile());
        }

        // mongoUtil.Close();
    }

    /**
     * 判断POIID是否存在，如果存在，需要比较入库的序列号<=库中的序列号，忽略不入库
     * 
     * @param json
     * @param oDB
     */
    public void writeToDB(CSVEntry newEntry) {
        if (newEntry == null) {
            return;
        }
        String provinceCode = newEntry.getProvinceCode();
        String poiid = newEntry.getPoiid();
        DBObject oldObj = MongoInstance.getInstance().queryByPoiid(poiid, provinceCode);
        if (oldObj == null) {
        	MongoInstance.getInstance().AddRecord(newEntry.getDBObject(), provinceCode);
        	POI_Insert_Count++;
        	
        	// 用于数据统计
        	DailyTotalTask.insert(provinceCode,DT_TYPE.INSERT);
        } else {
            long snNew = newEntry.getSerialNumber();
            long snOld = 0;
            Object sn = oldObj.get(DBField.SERIAL_NUMBER);
            if (sn instanceof Integer) {
                snOld = ((Integer)sn).intValue();
            } else if (sn instanceof Long) {
                snOld = ((Long)sn).intValue();
            }
            if (snOld < snNew) {
                DBObject saveObj = newEntry.getDBObject();
                
                if(checkIfChange(oldObj,saveObj,provinceCode)){
                	 saveObj.put(DBField.ID, oldObj.get(DBField.ID));
                     MongoInstance.getInstance().saveRecord(saveObj, provinceCode);
                     POI_Update_Count ++;
                     // 用于数据统计
                 	 DailyTotalTask.insert(provinceCode,DT_TYPE.UPDATE);
                }else{
                	POI_No_Update_Count ++;
                	 // 用于数据统计
                	 DailyTotalTask.insert(provinceCode,DT_TYPE.NOUPDATE);
                }
            }
        }
    }
    
    /**
     * 检测POI是否有改变
     * 
     * @param oldObj
     * @param saveObj
     * @return
     */
    public boolean checkIfChange(DBObject oldObj , DBObject saveObj,String provinceCode){
    	boolean ret = false;
    	int oldValid =  Integer.parseInt(oldObj.get(CSVField.IS_VALID).toString());
    	if(oldValid != 0){
    		POI_Error_Valid ++;
    		// 用于数据统计
    		DailyTotalTask.insert(provinceCode,DT_TYPE.REMOVE);
    		return true;
    	}
    	
    	String poiChangeInfo  = ConfigAccess.getInstance().POICHANGEINFO;
    	if(poiChangeInfo == null){
    		System.out.println("[poi change configer error]");
    	}
    	
    	String[] poiChanges = poiChangeInfo.split(",");
    	String key = null;

    	for (int i = 0; i < poiChanges.length; i++) {
			key =  poiChanges[i];
			if(oldObj.get(key) == null && saveObj.get(key) == null){
				ret = false;
			}else if(saveObj.get(key) != null &&  oldObj.get(key) != null && oldObj.get(key).toString().equals(saveObj.get(key).toString())){
				ret = false;
			}else{
				ret = true;
				break;
			}
		}
    	
    	return ret;
    }
    
    
    /**
     * 录入全量数据，不用查询
     * @param newEntry
     */
    public void writeToDBAll(CSVEntry newEntry) {
        if (newEntry == null) {
            return;
        }
        String provinceCode = newEntry.getProvinceCode();
        MongoInstance.getInstance().AddRecord(newEntry.getDBObject(), provinceCode);
    }
    
}
