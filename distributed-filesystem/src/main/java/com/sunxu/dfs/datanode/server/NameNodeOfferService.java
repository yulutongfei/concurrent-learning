package com.sunxu.dfs.datanode.server;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 14:12
 * 负责根一组NameNode进行通信的offerService
 */
public class NameNodeOfferService {

    /**
     * 负责跟NameNode主节点通信的serviceActor组件
     */
    private NameNodeServiceActor activeServiceActor;

    /**
     * 负责跟NameNode备节点通信的ServiceActor组件
     */
    private NameNodeServiceActor standbyServiceActor;

    /**
     * 这个dataNode上保持的ServiceActor列表
     */
    private CopyOnWriteArrayList<NameNodeServiceActor> serviceActors;

    public NameNodeOfferService() {
        this.activeServiceActor = new NameNodeServiceActor();
        this.standbyServiceActor = new NameNodeServiceActor();
        this.serviceActors = new CopyOnWriteArrayList<>();
        this.serviceActors.add(activeServiceActor);
        this.serviceActors.add(standbyServiceActor);
    }

    /**
     * 启动offerService
     */
    public void start() {
        // 直接使用两个组件分别向主备两个NameNode进行注册
        register();
    }

    private void register() {
        CountDownLatch latch = new CountDownLatch(2);
        this.activeServiceActor.register(latch);
        this.standbyServiceActor.register(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("主备2个NameNode全部注册完毕");
    }

    /**
     * 关闭指定的serviceActor
     *
     * @param serviceActor
     */
    public void shutdown(NameNodeServiceActor serviceActor) {
        this.serviceActors.remove(serviceActor);
    }

    /**
     * 迭代遍历serviceActors
     */
    public void iterateServiceActors() {
        Iterator<NameNodeServiceActor> iterator = serviceActors.iterator();
        while (iterator.hasNext()) {
            NameNodeServiceActor serviceActor = iterator.next();
        }
    }
}
