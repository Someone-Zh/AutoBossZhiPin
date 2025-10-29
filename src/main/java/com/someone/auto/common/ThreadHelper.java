package com.someone.auto.common;

import java.util.Random;

public class ThreadHelper {
    /**
     * 随机睡眠
     * @param seconds
     */
    public static void sleepByRandom(int seconds) {
        Random rand = new Random();
        int r = seconds * 1000 + rand.nextInt(1000);
        // 随机等待
        try {
            Thread.sleep(r);
        } catch (InterruptedException e) {  
        }
    }
}
