package com.greenit.integration;

public class TestProfile implements Runnable {
    private Object lock = new Object();
    private int throwException = 1;
    
    static {
        System.out.println("starting");
    }
    public void count1() {
        synchronized (lock) {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(new java.util.Random().nextInt(50));
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public void count2() throws Exception {
        synchronized (lock) {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(new java.util.Random().nextInt(20));
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if (throwException-- >= 0) {
                throw new Exception("");
            }
        }
        count1();
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new TestProfile());
        Thread t2 = new Thread(new TestProfile());
        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.exit(0);
    }

    public void run() {
        for (int i = 0; i < 5; i++) {
            count1();
            try {
                count2();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
