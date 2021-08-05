package com.sunxu.register.server;

import java.util.concurrent.atomic.AtomicLong;

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
     * 这个用synchronized上锁，性能其实是很差的
     * 因为可能会有很多线程，不断地接受到心跳的请求，就会来增加心跳次数
     * 多线程卡在这里，一个一个排队
     * 一次上锁，累加i，再次释放锁，会有一个问题
     * 如果你的服务实例很多的话，1万多个服务实例，没秒可能都会有很多请求过来更新心跳
     * 如果在这里加了synchronized的话，会影响并发的性能
     * 换成了AomicLong原子类之后，不加锁，无锁化，cas操作，保证原子性，还可以多线程并发
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
