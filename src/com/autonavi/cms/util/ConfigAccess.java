package com.autonavi.cms.util;

import java.util.Properties;

/**
 * load config file
 * 
 * @author shuai.qi
 *
 */
public class ConfigAccess {
	private static ConfigAccess instance = null;

	public String	BASE_HOST		= null;
	public int		BASE_PORT		= 0;
	public String	BASE_Name		= null;
	public String	PRODUCT_HOST	= null;
	public int		PRODUCT_PORT	= 0;
	public String	PRODUCT_Name	= null;
	public int      MODE            = 0;
	
	public String   POICHANGEINFO   = null;
	
	public String 	BASE_COUNT_NAME = null;
	public String 	BASE_COUNT_TABLE = null;

	private ConfigAccess() {
		Properties dbProps = new Properties();
		java.io.InputStream is = getClass().getResourceAsStream(
				"/webapp.properties");
		try {
			dbProps.load(is);
			this.BASE_HOST = dbProps.getProperty("base_host");
			this.BASE_PORT = Integer.parseInt(dbProps.getProperty("base_prot"));
			this.BASE_Name = dbProps.getProperty("base_name");
			
			this.BASE_COUNT_NAME =  dbProps.getProperty("base_count_name");
			this.BASE_COUNT_TABLE =  dbProps.getProperty("base_count_table");

//			this.PRODUCT_HOST = dbProps.getProperty("product_host");
//			this.PRODUCT_PORT = Integer.parseInt(dbProps
//					.getProperty("product_prot"));
//			this.PRODUCT_Name = dbProps.getProperty("product_name")
//					+ TimeUtil.getNowMMDD();
//			this.MODE = Integer.parseInt(dbProps
//					.getProperty("mode"));
			
			this.POICHANGEINFO =  dbProps.getProperty("poichangeinfo");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized ConfigAccess getInstance(){
		if(instance == null){
			instance = new ConfigAccess();
		}
		return instance;
	}

}