package com.sunxu.dfs.datanode.server;

import java.util.concurrent.CountDownLatch;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 14:12
 * 负责根一组NameNode进行通信的offerService
 */
public class NameNodeGroupOfferService {

    /**
     * 负责跟NameNode主节点通信的serviceActor组件
     */
    private NameNodeServiceActor activeServiceActor;

    /**
     * 负责跟NameNode备节点通信的ServiceActor组件
     */
    private NameNodeServiceActor standbyServiceActor;

    public NameNodeGroupOfferService() {
        this.activeServiceActor = new NameNodeServiceActor();
        this.standbyServiceActor = new NameNodeServiceActor();
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
}
