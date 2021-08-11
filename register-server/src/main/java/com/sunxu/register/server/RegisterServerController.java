package com.sunxu.register.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/13 23:21
 * 负责接受register-client发送过来的请求
 * <p>
 * Spring Cloud中的Eureka用的是jersey
 */
public class RegisterServerController {

    /**
     * 服务注册表
     */
    private final ServiceRegistry registry = ServiceRegistry.getInstance();

    /**
     * 服务注册表的缓存
     */
    private final ServiceRegistryCache registryCache = ServiceRegistryCache.getInstance();

    /**
     * 服务注册
     *
     * @param request 注册请求
     * @return 注册相应
     */
    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();
        try {
            // 在注册表加入这个服务实例
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setServiceName(request.getServiceName());
            serviceInstance.setIp(request.getIp());
            serviceInstance.setHostName(request.getHostName());
            serviceInstance.setPort(request.getPort());
            serviceInstance.setServiceInstanceId(request.getServiceId());

            registry.register(serviceInstance);

            // 更新自我保护机制的阈值
            synchronized (SelfProtectionPolicy.class) {
                SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
                selfProtectionPolicy.setExpectedHeartbeatRate(selfProtectionPolicy.getExpectedHeartbeatRate() + 2);
                selfProtectionPolicy.setExpectedHeartbeatThreshold((long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
            }

            // 过期掉注册表缓存
            registryCache.invalidate();
            response.setStatus(RegisterResponse.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(RegisterResponse.FAILURE);
        }
        return response;
    }

    /**
     * 服务实例下线
     *
     * @param serviceName
     * @param serviceInstanceId
     */
    public void cancel(String serviceName, String serviceInstanceId) {
        registry.remove(serviceName, serviceInstanceId);
        // 更新自我保护机制的阈值
        synchronized (SelfProtectionPolicy.class) {
            SelfProtectionPolicy selfProtectionPolicy = SelfProtectionPolicy.getInstance();
            selfProtectionPolicy.setExpectedHeartbeatRate(selfProtectionPolicy.getExpectedHeartbeatRate() - 2);
            selfProtectionPolicy.setExpectedHeartbeatThreshold((long) (selfProtectionPolicy.getExpectedHeartbeatRate() * 0.85));
        }

        // 过期掉注册表缓存
        registryCache.invalidate();
    }

    /**
     * 发送心跳
     *
     * @param request 心跳请求
     * @return 心跳响应
     */
    public HeartBeatResponse heartBeat(HeartBeatRequest request) {
        HeartBeatResponse response = new HeartBeatResponse();
        System.out.println("心跳请求request:" + request);
        try {
            // 对服务进行续约
            ServiceInstance serviceInstance = registry.getServiceInstance(request.getServiceName(), request.getServiceId());
            if (serviceInstance != null) {
                serviceInstance.renew();
            }

            // 记录一下每分钟心跳次数
            HeartbeatCounter heartbeatCounter = HeartbeatCounter.getInstance();
            heartbeatCounter.increment();
            response.setStatus(HeartBeatResponse.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HeartBeatResponse.FAILURE);
        }
        return response;
    }

    /**
     * 拉取全量服务注册表
     *
     * @return
     */
    public Applications fetchFullRegistry() {
        return (Applications) registryCache.get(ServiceRegistryCache.CacheKey.FULL_SERVICE_REGISTRY);
    }

    /**
     * 拉取增量服务注册表
     *
     * @return
     */
    public DeltaRegistry fetchDeltaRegistry() {
        return (DeltaRegistry) registryCache.get(ServiceRegistryCache.CacheKey.DELTA_SERVICE_REGISTRY);
    }
}
