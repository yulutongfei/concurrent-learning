package com.sunxu.register.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/10 11:24
 */
public class HttpSender {

    /**
     * 注册请求
     * @param request
     * @return
     */
    public RegisterResponse register(RegisterRequest request) {
        System.out.println(request + "正在注册");
        RegisterResponse response = new RegisterResponse();
        response.setStatus(RegisterResponse.SUCCESS);
        return response;
    }

    /**
     * 心跳请求
     * @param request
     * @return
     */
    public HeartBeatResponse heartBeat(HeartBeatRequest request) {
        System.out.println(request.getServiceId() + "正在发送心跳请求");
        HeartBeatResponse response = new HeartBeatResponse();
        response.setStatus(HeartBeatResponse.SUCCESS);
        return response;
    }

    /**
     * 服务实例下线
     * @param serviceName
     * @param serviceInstanceId
     */
    public void cancel(String serviceName, String serviceInstanceId) {
        System.out.println("服务实例下线[" + serviceName + ", " + serviceInstanceId + "]");
    }

    /**
     * 全量拉取服务注册表
     * @return
     */
    public Applications fetchFullRegistry() {
        Map<String, Map<String, ServiceInstance>> registry = new HashMap<>(8);
        Map<String, ServiceInstance> serviceInstances = new HashMap<>(8);
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceName("FINANCE-SERVICE");
        serviceInstance.setIp("192.168.31.207");
        serviceInstance.setHostName("finance-service-01");
        serviceInstance.setPort(9000);
        serviceInstance.setServiceInstanceId("FINANCE-SERVICE-192.168.31.207:9000");

        serviceInstances.put("FINANCE-SERVICE-192.168.31.207:9000", serviceInstance);
        registry.put("FINANCE-SERVICE", serviceInstances);
        System.out.println("拉取注册表: " + registry);
        return new Applications(registry);
    }

    /**
     * 增量拉取服务注册表
     * @return
     */
    public DeltaRegistry fetchDeltaRegistry() {
        LinkedList<CachedServiceRegistry.RecentlyChangedServiceInstance> recentlyChangedQueue
                = new LinkedList<>();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceName("ORDER-SERVICE");
        serviceInstance.setIp("192.168.31.211");
        serviceInstance.setHostName("order-service-01");
        serviceInstance.setPort(9000);
        serviceInstance.setServiceInstanceId("FINANCE-SERVICE-192.168.31.211:9000");

        CachedServiceRegistry.RecentlyChangedServiceInstance recentlyChangedItem
                = new CachedServiceRegistry.RecentlyChangedServiceInstance(serviceInstance,
                System.currentTimeMillis(),
                CachedServiceRegistry.ServiceInstanceOperation.REGISTER);
        recentlyChangedQueue.add(recentlyChangedItem);
        DeltaRegistry deltaRegistry = new DeltaRegistry(recentlyChangedQueue, 2L);
        System.out.println("拉取增量注册表: " + deltaRegistry);
        return deltaRegistry;
    }
}
