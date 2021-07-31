package com.sunxu.register.server;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/18 06:46
 */
public class HeartbeatCounter {

    private static HeartbeatCounter instance = new HeartbeatCounter();

    /**
     * 最近一分钟的心跳次数
     */
    private AtomicLong latestMinuteHeartbeatRate = new AtomicLong(0);
//    private LongAdder latestMinuteHeartbeatRate = new LongAdder();

    private long latestMinuteTimestamp = System.currentTimeMillis();

    private HeartbeatCounter() {
        Daemon daemon = new Daemon();
        daemon.setDaemon(true);
        daemon.start();
    }

    public static HeartbeatCounter getInstance() {
        return instance;
    }

    /**
     * 增加最近1分钟心跳次数
     */
    public void increment() {
        // 用synchronized上锁,性能会很低
//        latestMinuteHeartbeatRate.increment();
        latestMinuteHeartbeatRate.incrementAndGet();
    }

    /**
     * 返回最近1分钟心跳
     *
     * @return
     */
    public long get() {
        return latestMinuteHeartbeatRate.longValue();
    }

    private class Daemon extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - latestMinuteTimestamp > 60 * 1000) {
                        while (true) {
                            long expectedValue = latestMinuteHeartbeatRate.get();
                            if (latestMinuteHeartbeatRate.compareAndSet(expectedValue, 0L)) {
                                break;
                            }
                        }
//                        latestMinuteHeartbeatRate = new LongAdder();
                        latestMinuteTimestamp = System.currentTimeMillis();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
