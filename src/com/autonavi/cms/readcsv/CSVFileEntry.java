package com.autonavi.cms.readcsv;

import java.io.File;
import java.util.ArrayList;

public class CSVFileEntry {

    private File mFile;

    private ArrayList<CSVEntry> mCVSEntryList;

    public CSVFileEntry(File file) {
        mFile = file;
        mCVSEntryList = new ArrayList<CSVEntry>();
    }

    public void addData(String line) {
        CSVEntry entry = CSVEntry.parse(line);
        if (entry == null) {
            return;
        }
        mCVSEntryList.add(entry);
    }

    private void clear() {
        mCVSEntryList.clear();
        mFile = null;
    }

    public void deleteFile() {
        if (mFile.exists()) {
            mFile.delete();
        }
        clear();
    }
    
    public File getFile(){
    	return mFile;
    }

    public int getDataCount() {
        return mCVSEntryList.size();
    }

    public ArrayList<CSVEntry> getCSVEntryList() {
        return mCVSEntryList;
    }
}
