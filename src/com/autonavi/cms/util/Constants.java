package com.autonavi.cms.util;

public class Constants {
    private Constants() {
        // do nothing
    }

    public static interface CSVField {
        public static final String BASE = "base";
        public static final String CODE = "code";
        public static final String IS_VALID = "is_valid";
        public static final String PROVINCE_CODE = "province_code";
    }

    public static interface DBField {
        public static final String POIID = "base.poiid";
        public static final String SERIAL_NUMBER = "serialNumber";
        public static final String ID = "_id";
    }

    public static final int IS_VALID_VALUE_FALSE = 1;
    public static final int IS_VALID_VALUE_TRUE = 0;

    public static final int LOG_LIMIT = 100;
    public static final int LOG_READ_LIMIT = 100;

    public static final String SEPARATOR_TAB = "\t";
}
