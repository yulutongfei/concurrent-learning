package com.sunxu.register.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/8 00:13
 * 服务注册的缓存
 */
public class ServiceRegistryCache {

    private static final ServiceRegistryCache instance = new ServiceRegistryCache();

    /**
     * 缓存同步间隔
     */
    private static final Long CACHE_MAP_SYNC_INTERVAL = 30 * 1000L;
    /**
     * 内部锁
     * readWriteMap的锁
     */
    private final Object lock = new Object();
    /**
     * 实际的注册表
     */
    private ServiceRegistry registry = ServiceRegistry.getInstance();

    /**
     * 只读缓存(一级缓存)
     */
    private Map<String, Object> readOnlyMap = new HashMap<>();

    /**
     * 读写缓存(二级缓存)
     */
    private Map<String, Object> readWriteMap = new HashMap<>();

    /**
     * cache map同步后台线程
     */
    private CacheMapSyncDaemon cacheMapSyncDaemon;
    /**
     * 对readOnlyMap读写锁
     */
    private ReentrantReadWriteLock readOnlyMapLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = readOnlyMapLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = readOnlyMapLock.writeLock();

    /**
     * 构造函数
     */
    public ServiceRegistryCache() {
        // 启动缓存同步线程
        this.cacheMapSyncDaemon = new CacheMapSyncDaemon();
        this.cacheMapSyncDaemon.setDaemon(true);
        this.cacheMapSyncDaemon.start();
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static ServiceRegistryCache getInstance() {
        return instance;
    }

    /**
     * 过期掉对应的缓存
     */
    public void invalidate() {
        synchronized (lock) {
            readWriteMap.remove(CacheKey.FULL_SERVICE_REGISTRY);
            readWriteMap.remove(CacheKey.DELTA_SERVICE_REGISTRY);
        }
    }

    /**
     * 根据缓存key来获取数据
     * <p>
     * 系统刚启动的时候，会有一个线程来填充各级缓存的数据
     * 此后30秒，大家全部都是读缓存数据的，不会涉及到任何加锁的行为
     * 在这个过程中，如果有人更新注册表数据的话，一方面会对注册表本身加写锁，另外一方面对缓存加一个锁，那么会过期掉readWriteMap里的缓存。
     * 此时所有加的锁，是不会对高频的读请求有任何的锁的冲突和影响的。
     * <p>
     * 在写数据的期间，读数据不涉及任何读写锁的冲突，直接读的是cache数据
     * <p>
     * 有一个后台线程，可能会过30秒之后，对缓存加一个锁，同步2个map的数据，在这个过程中，实际上说也是不会对高频的读操作施加任何的影响的
     * <p>
     * 只有此时，会有线程感知到缓存数据是null，重新填充数据，重新填充数据的时候，会涉及到重新从服务注册表查数据，然后加读锁，此时就是一个线程
     * 加了读锁，而且是很快的行为，大量的降低了频繁的读操作，可能频繁的跟写操作，读写锁冲突的问题
     *
     * @param cacheKey
     * @return
     */
    public Object get(String cacheKey) {
        Object cacheValue;
        readLock.lock();
        try {
            cacheValue = readOnlyMap.get(cacheKey);
            // 系统刚启动
            if (cacheValue == null) {
                synchronized (lock) {
                    if (readOnlyMap.get(cacheKey) == null) {
                        cacheValue = readWriteMap.get(cacheKey);
                        if (cacheValue == null) {
                            cacheValue = getCacheValue(cacheKey);
                            readWriteMap.put(cacheKey, cacheValue);
                        }
                        readOnlyMap.put(cacheKey, cacheValue);
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        return cacheValue;
    }

    /**
     * 获取实际的缓存数据
     * <p>
     * 大幅度的降低了高频繁的读操作对服务注册表加读锁的请求，避免了频繁的服务注册表
     * 的读锁与写锁的冲突
     *
     * @param cacheKey
     * @return
     */
    public Object getCacheValue(String cacheKey) {
        registry.readLock();
        try {
            if (CacheKey.FULL_SERVICE_REGISTRY.equals(cacheKey)) {
                return new Applications(registry.getRegistry());
            } else if (CacheKey.DELTA_SERVICE_REGISTRY.equals(cacheKey)) {
                return registry.getDeltaRegistry();
            } else {
                return null;
            }
        } finally {
            registry.readUnLock();
        }
    }

    /**
     * 缓存key
     */
    public static class CacheKey {

        /**
         * 全量注册缓存key
         */
        public static final String FULL_SERVICE_REGISTRY = "full_service_registry";

        /**
         * 增量注册表缓存key
         */
        public static final String DELTA_SERVICE_REGISTRY = "delta_service_registry";
    }

    /**
     * 同步两个缓存Map的后台线程
     */
    class CacheMapSyncDaemon extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    writeLock.lock();
                    try {
                        synchronized (lock) {
                            if (readWriteMap.get(CacheKey.FULL_SERVICE_REGISTRY) == null) {
                                readOnlyMap.put(CacheKey.FULL_SERVICE_REGISTRY, null);
                            }
                            if (readWriteMap.get(CacheKey.DELTA_SERVICE_REGISTRY) == null) {
                                readOnlyMap.put(CacheKey.DELTA_SERVICE_REGISTRY, null);
                            }
                        }
                    } finally {
                        writeLock.unlock();
                    }
                    Thread.sleep(CACHE_MAP_SYNC_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
