package com.sunxu.register.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicStampedReference;

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
    private AtomicStampedReference<Applications> applications;

    /**
     * RegisterClient
     */
    private RegisterClient registerClient;

    /**
     * 通信组件
     */
    private HttpSender httpSender;

    /**
     * 代表当前本地缓存注册表的版本号
     */
    private AtomicLong applicationVersion = new AtomicLong(0);

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
        this.applications = new AtomicStampedReference<>(new Applications(), 0);
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
     * 获取服务注册表
     *
     * @return
     */
    public Map<String, Map<String, ServiceInstance>> getRegistry() {
        return applications.getReference().getRegistry();
    }

    private void fetchFullRegistry() {
        while (true) {
            // 一定要在发起网络请求之前,先拿到一个当时的版本号
            Long expectedApplicationVersion = applicationVersion.get();
            // 接着在这里发起网络请求,此时可能会有别的线程来修改这个注册表,在这个期间更新版本
            Applications fetchedApplications = httpSender.fetchFullRegistry();
            // 必须是发起网络请求之后,这个注册表的版本号没有人修改过,此时他才能去修改
            // 如果在这个期间,有人修改过注册表,版本不一样了,此时就直接if不成立,不要把你拉取到
            // 旧版本的注册表给设置进去
            if (applicationVersion.compareAndSet(expectedApplicationVersion, expectedApplicationVersion + 1)) {
                // 在jvm里,引用的赋值本身是保证原子性的
                // long i = 0
                // 通过AtomicReference
                Applications expectedApplications = applications.getReference();
                int expectedStamp = applications.getStamp();
                if (applications.compareAndSet(expectedApplications, fetchedApplications, expectedStamp, expectedStamp + 1)) {
                    return;
                }
            }
        }
    }

    /**
     * 负责定时拉取注册表到本地来进行缓存
     * <p>
     * 负责全量拉取注册表
     */
    private class FetchFullRegistryWorker extends Thread {

        @Override
        public void run() {
            // 拉取全量注册表
            // 这个操作要走网络,但是不知道为什么网络延迟,此时就是一直卡住了,数据没有返回回来
            // 此时的这个数据就是一个旧的版本,里面仅仅包含了30个服务实例
            // 全量拉全量注册的线程拿到数据,赋值了30个旧的服务实例
            fetchFullRegistry();
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
                    // 先拉了一个增量注册表,发现和本地合并后,发现条数不对
                    Long expectedVersion = applicationVersion.get();
                    DeltaRegistry deltaRegistry = httpSender.fetchDeltaRegistry();
                    if (applicationVersion.compareAndSet(expectedVersion, expectedVersion + 1)) {
                        // 一类是注册,一类是删除
                        // 如果是注册的话,就判断一下这个服务实例是否在这个本地缓存的注册表里
                        // 如果不在的话,就放到本地缓存的注册表里去
                        // 如果是删除的话,就看一下,如果服务实例存在,就给删除了
                        // 这里其实是大量修改本地缓存的注册表,所以此处需要加锁
                        mergeDeltaRegistry(deltaRegistry);
                        reconcileRegistry(deltaRegistry);
                    }

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
                Map<String, Map<String, ServiceInstance>> registry = applications.getReference().getRegistry();
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
            for (Map<String, ServiceInstance> serviceInstanceMap : applications.getReference().getRegistry().values()) {
                clientSideTotalCount += serviceInstanceMap.size();
            }
            if (!serverSideTotalCount.equals(clientSideTotalCount)) {
                // 如果数量不对,则重新全量拉取注册表进行纠正
                // 为什么使用AtomicReference,而不是用volatile呢,此时是先读在写回,类似与i++的操作,并不是原子性的操作
                // volatile并不保证原子性,故使用AtomicReference的CAS操作来保证操作的原子性

                // 重新拉取全量注册表进行纠正
                // 进行了全量注册表最新数据的一个赋值,可能包含了40个服务实例
                // 最新数据
                fetchFullRegistry();
            }
        }
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
