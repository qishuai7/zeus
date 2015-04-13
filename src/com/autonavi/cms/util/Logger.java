package com.autonavi.cms.util;

import org.apache.log4j.Priority;

public class Logger  { 
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Logger.class.getName());
     
    public Logger(Object sd){
    }

    public static void d(Object str){
    	log.debug(str);
    }
    
    public static void i(Object str){
    	log.info(str);
    }
    
    public static void warn(Object str){
    	log.warn(str);
    }
    
    public static void error(Object str){
    	log.error(str);
    }
    
    public static void main(String[] args) {
    	log.info("Start of the main() in TestLog4j"); 
    	log.info("Just testing a log message with priority set to INFO"); 
    	log.warn("Just testing a log message with priority set to WARN"); 
    	log.error("Just testing a log message with priority set to ERROR"); 
    	log.fatal("Just testing a log message with priority set to FATAL"); 
    	log.log(Priority.DEBUG, "Testing a log message use a alternate form"); 
    	log.debug("End of the main() in TestLog4j"); 
    } 
} 