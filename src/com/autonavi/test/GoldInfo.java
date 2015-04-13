
package com.autonavi.test;

public class GoldInfo implements Cloneable{

    public GoldInfo(String poiId, int id) {
        this.poiId = poiId;
        this.id = id;
    }

    public GoldInfo() {

    }

    public String poiId = null;
    public int id = 0;
    public GoldInfo g;
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
