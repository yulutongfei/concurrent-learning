package com.sunxu.register.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/18 07:15
 * 自我保护机制
 */
public class SelfProtectionPolicy {

    /**
     * 期望的一个心跳次数,如果你有10个服务实例,这个数值就是10 * 2 = 20
     */
    private long expectedHeartbeatRate = 0L;

    /**
     * 期望的心跳次数的阈值 10 * 2 * 0.85 = 17, 每分钟至少得有17次心跳,才不用进入自我保护机制
     */
    private long expectedHeartbeatThreshold = 0L;

    private static SelfProtectionPolicy instance = new SelfProtectionPolicy();

    private SelfProtectionPolicy() {

    }

    public static SelfProtectionPolicy getInstance() {
        return instance;
    }

    public long getExpectedHeartbeatRate() {
        return expectedHeartbeatRate;
    }

    public void setExpectedHeartbeatRate(long expectedHeartbeatRate) {
        this.expectedHeartbeatRate = expectedHeartbeatRate;
    }

    public long getExpectedHeartbeatThreshold() {
        return expectedHeartbeatThreshold;
    }

    public void setExpectedHeartbeatThreshold(long expectedHeartbeatThreshold) {
        this.expectedHeartbeatThreshold = expectedHeartbeatThreshold;
    }

    /**
     * 是否需要开启自我保护机制
     * @return
     */
    public Boolean isEnable() {
        HeartbeatCounter heartbeatCounter = HeartbeatCounter.getInstance();
        long latestMinuteHeartbeatRate = heartbeatCounter.get();
        if (latestMinuteHeartbeatRate < this.expectedHeartbeatThreshold) {
            System.out.println("[自我保护机制开启]最近一分钟心跳次数=" + latestMinuteHeartbeatRate +
                    ", 期望心跳次数=" + this.expectedHeartbeatThreshold);
            return true;
        }
        System.out.println("[自我保护机制未开启]最近一分钟心跳次数=" + latestMinuteHeartbeatRate +
                ", 期望心跳次数=" + this.expectedHeartbeatThreshold);
        return false;
    }
}
