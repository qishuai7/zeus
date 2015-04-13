package com.autonavi.cms.readcsv;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 队列块
 * 
 * @author shuai.qi
 * 
 */
public class QueueBlock {
	public static final int							QUEUESIZE	= 20;
	public static ArrayBlockingQueue<CSVFileEntry>	csvQueue	= new ArrayBlockingQueue<CSVFileEntry>(
																		QUEUESIZE);

}
