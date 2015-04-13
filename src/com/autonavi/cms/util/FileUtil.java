package com.autonavi.cms.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class FileUtil {
	public static void main(String[] args) {
//		ArrayList<String> list = new ArrayList<String>();
//		FileUtil.getFilePath("D://file_product", list);
//		System.out.println(list.get(0));
//		System.out.println(list.get(1));
//		System.out.println(list.get(2));
		// D:\file_product\2508\cmstest.csv
		// D:\file_product\2509\2359999_c215078b68a489da5961ada68eea1cd2.csv
		// D:\file_product\2510\2360000_6e612a7f9bea1d97ac84a3eb45b9d67c.csv
		
//		setErrorLog("D://error.log", "1");
//		setErrorLog("D://error.log", "2");
//		setErrorLog("D://error.log", "3");
//		String sourceFolder = "D://file_product";
//		ArrayList<File> csvList = new ArrayList<File>();
//		FileUtil.getFilePath(sourceFolder, csvList,"20140604000000");//"20140604000000"
//		File[] csvFileArray = sortFile(csvList);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	
//		
//		for (File file : csvFileArray) {
//			String date = sdf.format(new Date(file.lastModified()));
//			System.out.println(date);
//		}
//		System.out.println(System.currentTimeMillis());
		
		System.out.println(checkIsDirectory(null));
		
	}
	
	public static boolean checkIsDirectory(String sourceFolder){
		boolean isDirectory = false;
		if(sourceFolder != null){
			File file = new File(sourceFolder);
			if(file.isDirectory()){
				isDirectory = true;
			}
		}
		
		return isDirectory;
	}
	
	public static long getTime(String timeStr){
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		Date dDate = null;
		try {
			dDate = format.parse(timeStr);
		} catch (ParseException e) {
			System.out.println("日期错误...");
			e.printStackTrace();
		}
		
		return dDate.getTime();
	}

	/**
	 * 获取摸个文件夹下所有的csv文件,按文件日期排序
	 * 
	 * @param path
	 * @return
	 */
	public static void getFilePath(String path, ArrayList<File> list,String endTime) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] dirFile = file.listFiles();
			for (File f : dirFile) {
				if (f.isDirectory()){
					getFilePath(f.getAbsolutePath(), list, endTime);
				}
				else {
					if (f.getName().endsWith(".csv")) {
						File csvFile = new File(f.getAbsolutePath());
						if(csvFile.isFile()){
							if(endTime == null){ 
								list.add(csvFile);
							}else{
								long end = getTime(endTime);
								if(csvFile.lastModified() <= end){
									list.add(csvFile);
								}
							}
						}

					}

				}
			}
		}
	}
	
	/**
	 * 按日期排序文件
	 * 
	 * @param list
	 * @return
	 */
	public static File[] sortFile(ArrayList<File> list){
		if(list == null){
			return null;
		}
		File[] fileArray =new File[list.size()]; 
		list.toArray(fileArray);
		Arrays.sort(fileArray, new FileUtil.CompratorByLastModified());  
	
		return fileArray; 
	}
	
	
	
	static class CompratorByLastModified implements Comparator<File>  
	  {  
		   public int compare(File f1, File f2) {  
		    long diff = f1.lastModified()-f2.lastModified();  
		        if(diff>0)  
		          return 1;  
		        else if(diff==0)  
		          return 0;  
		        else  
		          return -1;  
		  }  
		  public boolean equals(Object obj){  
		    return true;  
		  }  
	  }  
	
	public static void deleteFile(String path){
		File f = new File(path);  // 输入要删除的文件位置
		if(f.exists()){
			f.delete();
		}
	}

	/**
	 * 获取存放上一次处理的最新文件Path
	 * 
	 * @param path
	 * @return
	 */
	public static String getPreProcessPath(String path) {
		String prePath = null;
		try {
			File file = new File(path);
			if(!file.exists()){
				file.mkdir();
			}
			BufferedReader br = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = br.readLine()) != null) {
				prePath = line;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prePath;
	}

	/**
	 * 获取存放上一次处理的最新文件Path
	 * 
	 * @param path
	 * @return
	 */
	public static String setPreProcessPath(String path,String str) {
		String prePath = null;
		BufferedWriter br = null;
		try {
			File file = new File(path);
			br = new BufferedWriter(new FileWriter(file));

			br.write(str);
			br.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.flush();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		return prePath;
	}
	
	/**
	 * 创建文件夹和文件
	 * @param path
	 * @param str
	 */
	public static void writeToFile(String path,String str) {
		FileWriter writer = null;
		
		if(path == null || str == null || path.lastIndexOf(File.separator) == -1){
			return;
		}
		
		try {
			String dirPath = path.substring(0,path.lastIndexOf(File.separator));
			File dirFile = new File(dirPath);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			
			File file = new File(path);
			if(!file.exists()){
				file.createNewFile();
			}
			
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件      
            writer = new FileWriter(path, true);  
            writer.write(str);                    
            writer.write("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
