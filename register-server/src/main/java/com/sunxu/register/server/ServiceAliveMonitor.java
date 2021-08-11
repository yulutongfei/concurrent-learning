package com.sunxu.register.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/14 00:39
 * 微服务存活状态监控组建
 */
public class ServiceAliveMonitor {

    /**
     * 检查服务存活间隔
     */
    private static final Long CHECK_ALIVE_INTERVAL = 60 * 1000L;

    private final ServiceRegistryCache registryCache = ServiceRegistryCache.getInstance();

    private final Daemon daemon;

    public ServiceAliveMonitor() {
        daemon = new Daemon();
        daemon.setDaemon(true);
    }

    public void start() {
        daemon.start();
    }
    /**
     * 负责监控微服务存活状态的后台线程
     */
    private class Daemon extends Thread {

        private final ServiceRegistry serviceRegistry = ServiceRegistry.getInstance();

        @Override
        public void run() {
            Map<String, Map<String, ServiceInstance>> registryMap;
            while (true) {
                try {
                    // 是否开启自我保护机制
                    SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
                    if (selfProtectionPolicy.isEnable()) {
                        Thread.sleep(CHECK_ALIVE_INTERVAL);
                        continue;
                    }
                    // 定义要删除的服务实例的集合
                    List<ServiceInstance> removingServiceInstances = new ArrayList<>();
                    // 开始读服务注册表的数据，这个过程中，别人是可以读，但是不可以写
                    // 对整个服务注册表加读锁
                    serviceRegistry.readLock();
                    try {
                        registryMap = serviceRegistry.getRegistry();
                        for (String serviceName : registryMap.keySet()) {
                            Map<String, ServiceInstance> instanceMap = registryMap.get(serviceName);
                            for (ServiceInstance serviceInstance : instanceMap.values()) {
                                if (!serviceInstance.isAlive()) {
                                    // 说明服务实例距离上一次发送心跳已经超过90秒了
                                    removingServiceInstances.add(serviceInstance);
                                }
                            }
                        }
                    } finally {
                        // 释放读锁
                        serviceRegistry.readUnLock();
                    }
                    // 将所有的要删除的服务实例，从服务注册表删除
                    for (ServiceInstance serviceInstance : removingServiceInstances) {
                        // 从注册表中摘除服务实例
                        serviceRegistry.remove(serviceInstance.getServiceName(), serviceInstance.getServiceInstanceId());
                        // 更新自我保护机制的阈值
                        synchronized (SelfProtectionPolicy.class) {
                            selfProtectionPolicy.setExpectedHeartbeatRate(selfProtectionPolicy.getExpectedHeartbeatRate() - 2);
                            selfProtectionPolicy.setExpectedHeartbeatThreshold((long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
                        }
                    }

                    // 过期掉注册表缓存
                    if (removingServiceInstances.size() != 0) {
                        registryCache.invalidate();
                    }
                    Thread.sleep(CHECK_ALIVE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
