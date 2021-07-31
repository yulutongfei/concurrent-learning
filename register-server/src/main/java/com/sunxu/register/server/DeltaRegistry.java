package com.sunxu.register.server;

import java.util.LinkedList;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/30 07:20
 * 增量注册表
 */
public class DeltaRegistry {

    private LinkedList<ServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue;

    private Long serviceInstanceTotalCount;

    public DeltaRegistry(LinkedList<ServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue, Long serviceInstanceTotalCount) {
        this.recentlyChangeQueue = recentlyChangeQueue;
        this.serviceInstanceTotalCount = serviceInstanceTotalCount;
    }

    public LinkedList<ServiceRegistry.RecentlyChangedServiceInstance> getRecentlyChangeQueue() {
        return recentlyChangeQueue;
    }

    public void setRecentlyChangeQueue(LinkedList<ServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue) {
        this.recentlyChangeQueue = recentlyChangeQueue;
    }

    public Long getServiceInstanceTotalCount() {
        return serviceInstanceTotalCount;
    }

    public void setServiceInstanceTotalCount(Long serviceInstanceTotalCount) {
        this.serviceInstanceTotalCount = serviceInstanceTotalCount;
    }
}
