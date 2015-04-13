package com.autonavi.cms.convert;

import java.util.concurrent.ArrayBlockingQueue;

import com.mongodb.DBObject;

public class MonitorTask implements Runnable{
	
	private String taskName = null;
	private ArrayBlockingQueue<DBObject> queue = null;
	
	public MonitorTask(String taskName,ArrayBlockingQueue<DBObject> queue) {
		this.taskName = taskName;
		this.queue = queue;
	}


	@Override
	public void run() {
		if(queue == null){
			return ;
		}
		
		while (true) {
			System.out.println(taskName + "当前队列size:" + queue.size());
			try {
				Thread.sleep(10*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
