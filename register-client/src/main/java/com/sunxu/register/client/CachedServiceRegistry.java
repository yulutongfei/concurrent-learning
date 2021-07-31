package com.sunxu.register.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/17 23:33
 * 服务注册中心的客户端缓存的注册表
 */
public class CachedServiceRegistry {

    public static final long SERVICE_REGISTRY_FETCH_INTERVAL = 30 * 1000;

    /**
     * 负责增量定时拉去注册表到本地的线程
     */
    private FetchDeltaRegistryWorker fetchDeltaRegistryWorker;

    /**
     * 客户端缓存的所有的服务实例信息
     */
    private AtomicReference<Applications> applications = new AtomicReference<>(new Applications());

    /**
     * RegisterClient
     */
    private RegisterClient registerClient;

    /**
     * 通信组件
     */
    private HttpSender httpSender;

    /**
     * 构造函数
     *
     * @param registerClient
     * @param httpSender
     */
    public CachedServiceRegistry(RegisterClient registerClient, HttpSender httpSender) {
        this.fetchDeltaRegistryWorker = new FetchDeltaRegistryWorker();
        this.registerClient = registerClient;
        this.httpSender = httpSender;
    }

    /**
     * 初始化
     */
    public void initialize() {
        // 启动全量拉取注册表的线程
        FetchFullRegistryWorker fetchFullRegistryWorker = new FetchFullRegistryWorker();
        fetchFullRegistryWorker.start();
        // 启动增量拉取注册表的线程
        this.fetchDeltaRegistryWorker.start();
    }

    /**
     * 销毁组件
     */
    public void destroy() {
        this.fetchDeltaRegistryWorker.interrupt();
    }

    /**
     * 负责定时拉取注册表到本地来进行缓存
     * <p>
     * 负责全量拉取注册表
     */
    private class FetchFullRegistryWorker extends Thread {

        @Override
        public void run() {
            // 启动拉取一次就可以了
            Applications fetchedApplications = httpSender.fetchFullRegistry();
            while (true) {
                Applications expectedApplications = applications.get();
                if (applications.compareAndSet(expectedApplications, fetchedApplications)) {
                    break;
                }
            }
        }
    }

    /**
     * 负责定时拉取注册表到本地来进行缓存
     * <p>
     * 负责增量拉取注册表
     */
    private class FetchDeltaRegistryWorker extends Thread {

        @Override
        public void run() {
            while (registerClient.isRunning()) {
                try {
                    Thread.sleep(SERVICE_REGISTRY_FETCH_INTERVAL);

                    // 拉取回来最近3分钟的服务实例变更
                    DeltaRegistry deltaRegistry = httpSender.fetchDeltaRegistry();
                    // 一类是注册,一类是删除
                    // 如果是注册的话,就判断一下这个服务实例是否在这个本地缓存的注册表里
                    // 如果不在的话,就放到本地缓存的注册表里去
                    // 如果是删除的话,就看一下,如果服务实例存在,就给删除了
                    // 这里其实是大量修改本地缓存的注册表,所以此处需要加锁
                    mergeDeltaRegistry(deltaRegistry);
                    reconcileRegistry(deltaRegistry);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 合并增量注册表到本地缓存注册表
         *
         * @param deltaRegistry
         */
        private void mergeDeltaRegistry(DeltaRegistry deltaRegistry) {
            synchronized (applications) {
                Map<String, Map<String, ServiceInstance>> registry = applications.get().getRegistry();
                for (RecentlyChangedServiceInstance recentlyChangedItem : deltaRegistry.getRecentlyChangeQueue()) {
                    // 如果是注册操作的话
                    String serviceInstanceId = recentlyChangedItem.serviceInstance.getServiceInstanceId();
                    String serviceName = recentlyChangedItem.serviceInstance.getServiceName();
                    if (ServiceInstanceOperation.REGISTER.equals(recentlyChangedItem.serviceInstanceOperation)) {
                        Map<String, ServiceInstance> serviceInstanceMap = registry.computeIfAbsent(serviceName, k -> new HashMap<>());
                        serviceInstanceMap.computeIfAbsent(serviceInstanceId, k -> recentlyChangedItem.serviceInstance);
                    } else if (ServiceInstanceOperation.REMOVE.equals(recentlyChangedItem.serviceInstanceOperation)) {
                        // 如果是删除操作的话
                        Map<String, ServiceInstance> serviceInstanceMap = registry.get(recentlyChangedItem.serviceInstanceOperation);
                        if (serviceInstanceMap != null) {
                            serviceInstanceMap.remove(serviceInstanceId);
                        }
                    }
                }
            }
        }

        /**
         * 校对调整注册表
         *
         * @param deltaRegistry
         */
        private void reconcileRegistry(DeltaRegistry deltaRegistry) {
            // 再检查一下,根服务端的注册表的服务实例的数量相比,是否是一致的
            // 封装一下增量注册表的对象,也就是拉取增量注册表的时候,一方面是返回那个数量,另一方面要返回服务端的服务实例数量
            Long serverSideTotalCount = deltaRegistry.getServiceInstanceTotalCount();
            long clientSideTotalCount = 0L;
            for (Map<String, ServiceInstance> serviceInstanceMap : applications.get().getRegistry().values()) {
                clientSideTotalCount += serviceInstanceMap.size();
            }
            if (!serverSideTotalCount.equals(clientSideTotalCount)) {
                // 如果数量不对,则重新全量拉取注册表进行纠正
                // 为什么使用AtomicReference,而不是用volatile呢,此时是先读在写回,类似与i++的操作,并不是原子性的操作
                // volatile并不保证原子性,故使用AtomicReference的CAS操作来保证操作的原子性
                Applications fetchedApplications = httpSender.fetchFullRegistry();
                while (true) {
                    Applications expectedApplications = applications.get();
                    if (applications.compareAndSet(expectedApplications, fetchedApplications)) {
                        break;
                    }
                }
            }
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

    /**
     * 获取服务注册表
     *
     * @return
     */
    public Map<String, Map<String, ServiceInstance>> getRegistry() {
        return applications.get().getRegistry();
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

        RecentlyChangedServiceInstance(ServiceInstance serviceInstance,
                                       Long changedTimestamp,
                                       String serviceInstanceOperation) {
            this.serviceInstance = serviceInstance;
            this.changedTimestamp = changedTimestamp;
            this.serviceInstanceOperation = serviceInstanceOperation;
        }
    }
}
