package com.autonavi.cms.main;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.autonavi.cms.util.ConfigAccess;
import com.autonavi.cms.util.MD5Util;
import com.autonavi.cms.util.Constants.CSVField;
import com.autonavi.cms.util.FileUtil;
import com.autonavi.cms.util.TimeUtil;
import com.mongodb.DBObject;

public class MainTest {
	public static int POIDUPCOUNT = 0;
	public static String ADCODE = null;

	final static String Name = "name";
	final static String Address = "address";
	final static String X = "x";
	final static String Y = "y";
	public final static HashMap<Integer, Integer> sType2DrawableHasChecked = new HashMap<Integer, Integer>();
	   static {
	        sType2DrawableHasChecked.put(1, 1111);
	        sType2DrawableHasChecked.put(2, 2222);
	        sType2DrawableHasChecked.put(3,333);
//	      sType2Drawable.put(TASK_LEVEL_TYPE.LEVEL_4, R.drawable.level_2);
	    }
	public static void main(String[] args) throws InterruptedException {
	    System.out.println(sType2DrawableHasChecked.get("df"));
//	    System.err.println("\t"+ "dfdsfsdf");
//		System.out.println(getScaleWidth(7.320389f) + " = " + getDesc(7.320389f));
//		System.out.println(getScaleWidth(14.640778f) + " = " + getDesc(14.640778f));
//		System.out.println(getScaleWidth(29.281555f) + " = " + getDesc(29.281555f));
//		System.out.println("lengths = " + lengths.length + "; scales" + scales.length);
//	    String str1 = "hello"; 
//	    String str2 = "he" + new String("llo"); 
//	    System.err.println(str1 == str2);
//	    File file = new File("D:\\GXD_122.apk");
//	    System.out.println(file.exists());
//	    System.out.println(getFileMD5(file) + "; size = " +getFileMD5(file).length());
//	    System.out.println(MD5Util.getFileMD5(file));
//	    StringBuffer sb = new StringBuffer();
//	    boolean f = sb.toString().equals("");
//	    System.out.println(f);
//	    AESHelper.getInstance().decompressAndUnzip(jsonObjectHeader.optString("data"));
	    /*for (;;) {
	        
	        System.out.println(TimeUtil.getNowTime());
	    }*/
//	    String str1 = "hello"; 
//	    String str2 = "he" + new String("llo"); 
//	    System.err.println(str1 == str2);
//	    System.out.println(1<<1);

	}
	
	public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        String fileMd5 = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        fileMd5 = bigInt.toString(16);
        if (fileMd5.length() < 32) { // 当生成的md5长度不够32位时，开头补零
            String firstZeros = "";
            for (int i = 0; i < 32 - fileMd5.length(); i++) {
                firstZeros = "0" + firstZeros;
            }
            fileMd5 = firstZeros + fileMd5;
        }

        return fileMd5;
    }
	

	private final static int[] lengths = { 32, 26, 26, 25, 20, 20, 20, 25, 32,
		32, 33, 26, 26, 28, 26, 26, 26, 26, 26, 26 };
	private final static int[] scales = { 5000000, 2000000, 1000000, 500000,
		200000, 100000, 50000, 30000, 20000, 10000, 5000, 2000, 1000, 500,
		200, 100, 50, 25, 10, 5 };
	private final static int MiddleScaleWidth = 50;

	/**
	 * 根据参数计算比例尺长度
	 * 
	 * @param scaleImg
	 * @return
	 */
	public static int getScaleWidth(float scaleImg) {
		int width = 0;
		int length = -1;
		float middleWidth = MiddleScaleWidth * scaleImg;
		if (middleWidth <= scales[scales.length - 1]) {
			length = scales[scales.length - 1];
		} else if (middleWidth >= scales[0]) {
			length = scales[0];

			return (int) (scales[0] / scaleImg);
		} else {
			for (int i = 0; i < scales.length; i++) {
				if (middleWidth >= scales[i]) {
					if ((scales[i - 1] - middleWidth) > middleWidth - scales[i]) {
						length = scales[i];
					} else {
						length = scales[i - 1];
					}

					break;
				}
			}
		}

		width = (int) (length / scaleImg);

		return width;
	}

	public static String getDesc(float scaleImg) {
		int length = -1;
		float middleWidth = MiddleScaleWidth * scaleImg;
		if (middleWidth <= scales[scales.length - 1]) {
			length = scales[scales.length - 1];
		} else if (middleWidth >= scales[3]) {
			length = scales[3];
		} else {
			for (int i = 0; i < scales.length; i++) {
				if (middleWidth >= scales[i]) {
					if ((scales[i - 1] - middleWidth) > middleWidth - scales[i]) {
						length = scales[i];
					} else {
						length = scales[i - 1];
					}

					break;
				}
			}
		}

		if (length % 1000 == 0) {
			int kmScale = length / 1000;
			return kmScale + "公里";
		} else {
			return length + "米";
		}
	}

	/**
	 * 检测POI是否有改变
	 * 
	 * @param oldObj
	 * @param saveObj
	 * @return
	 */
	public static boolean checkIfChange(DBObject oldObj, DBObject saveObj) {
		boolean ret = false;
		int oldValid = Integer.parseInt(oldObj.get(CSVField.IS_VALID)
				.toString());
		if (oldValid != 0) {
			return true;
		}

		String poiChangeInfo = ConfigAccess.getInstance().POICHANGEINFO;
		if (poiChangeInfo == null) {
			System.out.println("[poi change configer error]");
		}

		String[] poiChanges = poiChangeInfo.split(",");
		String key = null;

		for (int i = 0; i < poiChanges.length; i++) {
			key = poiChanges[i];
			if (oldObj.get(key) == null && saveObj.get(key) == null) {
				ret = false;
				continue;
			} else if (oldObj.get(key) == null && saveObj.get(key) != null) {
				ret = true;
				break;
			} else if (!oldObj.get(key).toString()
					.equals(saveObj.get(key).toString())) {
				ret = true;
				break;
			}
		}

		return ret;
	}

	/**
	 * 判断POI是否需要更新
	 * 
	 * @param newPoiInfo
	 * @param oldPoiInfo
	 * @return
	 */
	public static boolean comparePOIChange(DBObject newPoiInfo,
			DBObject oldPoiInfo) {
		if (newPoiInfo == null || oldPoiInfo == null) {
			return false;
		}

		/**
		 * Name，X,Y 非空
		 */
		if (!oldPoiInfo.get(Name).equals(newPoiInfo.get(Name))) {
			return false;
		}
		if (!oldPoiInfo.get(X).equals(newPoiInfo.get(X))) {
			return false;
		}
		if (!oldPoiInfo.get(Y).equals(newPoiInfo.get(Y))) {
			return false;
		}

		if (oldPoiInfo.get(Address) == null) {
			if (newPoiInfo.get(Address) == null) {
				return true;
			} else {
				return false;
			}
		} else if (!oldPoiInfo.get(Address).equals(newPoiInfo.get(Address))) {
			return false;
		}

		return true;
	}

	public static void convert() {
		String beginDate = "1407881254000";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String sd = sdf.format(new Date(Long.parseLong(beginDate)));

		System.out.println(sd);
	}

}
