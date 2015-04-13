package com.autonavi.cms.main;

import com.autonavi.vo.DaliyTotal;

public class TestMe {

    public static void main(String[] args) {
        Thread t = new Thread() {
            public void run(){
                 pong(); 
            }
        };

    }

    class a extends Thread{
        @Override
        public void run() {
            
        }
    }
    
    class b implements Runnable{

        @Override
        public void run() {
           
            
        }
        
    }
    
    static void pong() {
        System.out.print("pong");
        }

}
