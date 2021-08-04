package com.sunxu.register.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/13 23:25
 */
public class ServiceRegistry {

    public static final Long RECENTLY_CHANGED_ITEM_EXPIRED = 3 * 60 * 1000L;

    public static final Long RECENTLY_CHANGED_ITEM_CHECK_INTERVAL = 3000L;

    /**
     * 注册表是个单例
     */
    private static ServiceRegistry instance = new ServiceRegistry();

    /**
     * 核心内存数据结构:注册表
     * Map: key是服务名称, value是所有这个服务实例
     * Map<String, ServiceInstance> : key是服务实例id, value是服务实例的信息
     */
    private Map<String, Map<String, ServiceInstance>> registry
            = new HashMap<>();

    /**
     * 最近服务更新的队列
     */
    private LinkedList<RecentlyChangedServiceInstance> recentlyChangedQueue =
            new LinkedList<>();

    /**
     * 服务注册表的锁
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 读锁
     */
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    /**
     * 写锁
     */
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * 构造函数
     */
    private ServiceRegistry() {
        // 启动后台监控最近变更队列线程
        RecentlyChangedQueueMonitor recentlyChangedQueueMonitor =
                new RecentlyChangedQueueMonitor();
        recentlyChangedQueueMonitor.setDaemon(true);
        recentlyChangedQueueMonitor.start();
    }

    /**
     * 服务注册
     *
     * @param serviceInstance
     */
    public void register(ServiceInstance serviceInstance) {
        try {
            // 加写锁
            this.writeLock();
            // 将服务实例放入最新变更的队列中
            RecentlyChangedServiceInstance recentlyChangedItem = new RecentlyChangedServiceInstance(serviceInstance,
                    System.currentTimeMillis(), ServiceInstanceOperation.REGISTER);
            recentlyChangedQueue.offer(recentlyChangedItem);

            // 将服务实例放入注册表
            Map<String, ServiceInstance> serviceInstanceMap = registry.computeIfAbsent(serviceInstance.getServiceName(), k -> new HashMap<>());
            serviceInstanceMap.put(serviceInstance.getServiceInstanceId(), serviceInstance);

            System.out.println("服务实例[" + serviceInstance + "], 完成注册...");
            System.out.println("注册表registry:" + registry);
        } finally {
            // 释放写锁
            this.writeUnLock();
        }
    }

    /**
     * 从注册表中移除一个serviceInstance
     *
     * @param serviceName
     * @param serviceId
     */
    public void remove(String serviceName, String serviceId) {
        try {
            // 加写锁
            this.writeLock();
            System.out.println("服务实例[" + serviceName + ":" + serviceId + "],从注册表摘除");
            // 获取服务实例
            Map<String, ServiceInstance> serviceInstanceMap = registry.get(serviceName);
            ServiceInstance serviceInstance = serviceInstanceMap.get(serviceId);
            // 将服务实例放入最新变更的队列中
            RecentlyChangedServiceInstance recentlyChangedItem = new RecentlyChangedServiceInstance(serviceInstance,
                    System.currentTimeMillis(), ServiceInstanceOperation.REMOVE);
            recentlyChangedQueue.offer(recentlyChangedItem);

            // 从服务注册表删除服务实例
            serviceInstanceMap.remove(serviceId);
        } finally {
            // 释放写锁
            this.writeUnLock();
        }
    }

    /**
     * 最近变更队列的监控线程
     */
    class RecentlyChangedQueueMonitor extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (instance) {
                        RecentlyChangedServiceInstance recentlyChangedItem = null;
                        Long currentTimestamp = System.currentTimeMillis();
                        while ((recentlyChangedItem = recentlyChangedQueue.peek()) != null) {
                            // 判断如果一个服务实例变更信息已经在队列中超过3分钟了
                            // 就从队列删除
                            if (currentTimestamp - recentlyChangedItem.changedTimestamp > RECENTLY_CHANGED_ITEM_EXPIRED) {
                                recentlyChangedQueue.pop();
                            }
                        }
                    }
                    Thread.sleep(RECENTLY_CHANGED_ITEM_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取服务实例
     *
     * @param serviceName
     * @param serviceInstanceId
     * @return
     */
    public ServiceInstance getServiceInstance(String serviceName, String serviceInstanceId) {
        try {
            this.readLock();
            Map<String, ServiceInstance> serviceInstanceMap = registry.get(serviceName);
            return serviceInstanceMap.get(serviceInstanceId);
        } finally {
            this.readUnLock();
        }
    }

    /**
     * 加读锁
     */
    public void readLock() {
        this.readLock.lock();
    }

    /**
     * 释放读锁
     */
    public void readUnLock() {
        this.readLock.unlock();
    }

    public static ServiceRegistry getInstance() {
        return instance;
    }

    /**
     * 加写锁
     */
    public void writeLock() {
        this.writeLock.lock();
    }

    /**
     * 释放写锁
     */
    public void writeUnLock() {
        this.writeLock.unlock();
    }

    /**
     * 获取整个注册表
     *
     * @return
     */
    public Map<String, Map<String, ServiceInstance>> getRegistry() {
        return registry;
    }

    /**
     * 获取最近有变化的注册表
     *
     * @return
     */
    public DeltaRegistry getDeltaRegistry() {
        long totalCount = 0L;
        for (Map<String, ServiceInstance> serviceInstanceMap : registry.values()) {
            totalCount += serviceInstanceMap.size();
        }
        return new DeltaRegistry(recentlyChangedQueue, totalCount);
    }

    /**
     * 获取服务实例的总数
     *
     * @return
     */
    public Long getServiceInstanceTotalCount() {
        long totalCount = 0L;
        for (Map<String, ServiceInstance> serviceInstanceMap : registry.values()) {
            totalCount += serviceInstanceMap.size();
        }
        return totalCount;
    }

    /**
     * 最近变化的服务实例
     */
    static class RecentlyChangedServiceInstance {

        /**
         * 服务实例
         */
        ServiceInstance serviceInstance;
        /**
         * 发生变更的时间戳
         */
        Long changedTimestamp;

        /**
         * 变更操作
         */
        String serviceInstanceOperation;

        public RecentlyChangedServiceInstance(ServiceInstance serviceInstance,
                                              Long changedTimestamp,
                                              String serviceInstanceOperation) {
            this.serviceInstance = serviceInstance;
            this.changedTimestamp = changedTimestamp;
            this.serviceInstanceOperation = serviceInstanceOperation;
        }
    }

    /**
     * 服务实例操作
     */
    static class ServiceInstanceOperation {

        /**
         * 注册
         */
        public static final String REGISTER = "register";
        /**
         * 删除
         */
        public static final String REMOVE = "remove";
    }
}
