package com.sunxu.register.server;

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
                    registryMap = serviceRegistry.getRegistry();
                    for (String serviceName : registryMap.keySet()) {
                        Map<String, ServiceInstance> instanceMap = registryMap.get(serviceName);
                        for (ServiceInstance serviceInstance : instanceMap.values()) {
                            if (!serviceInstance.isAlive()) {
                                // 说明服务实例距离上一次发送心跳已经超过90秒了
                                // 从注册表中摘除服务实例
                                serviceRegistry.remove(serviceInstance.getServiceName(), serviceInstance.getServiceInstanceId());
                                // 更新自我保护机制的阈值
                                synchronized (SelfProtectionPolicy.class) {
                                    selfProtectionPolicy.setExpectedHeartbeatRate(selfProtectionPolicy.getExpectedHeartbeatRate() - 2);
                                    selfProtectionPolicy.setExpectedHeartbeatThreshold((long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
                                }
                            }
                        }
                    }
                    Thread.sleep(CHECK_ALIVE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
