package com.sunxu.register.client;

import java.util.LinkedList;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/30 07:20
 * 增量注册表
 */
public class DeltaRegistry {

    private LinkedList<CachedServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue;

    private Long serviceInstanceTotalCount;

    public DeltaRegistry(LinkedList<CachedServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue, Long serviceInstanceTotalCount) {
        this.recentlyChangeQueue = recentlyChangeQueue;
        this.serviceInstanceTotalCount = serviceInstanceTotalCount;
    }

    public LinkedList<CachedServiceRegistry.RecentlyChangedServiceInstance> getRecentlyChangeQueue() {
        return recentlyChangeQueue;
    }

    public void setRecentlyChangeQueue(LinkedList<CachedServiceRegistry.RecentlyChangedServiceInstance> recentlyChangeQueue) {
        this.recentlyChangeQueue = recentlyChangeQueue;
    }

    public Long getServiceInstanceTotalCount() {
        return serviceInstanceTotalCount;
    }

    public void setServiceInstanceTotalCount(Long serviceInstanceTotalCount) {
        this.serviceInstanceTotalCount = serviceInstanceTotalCount;
    }
}
