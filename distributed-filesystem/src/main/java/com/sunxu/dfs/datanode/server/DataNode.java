package com.sunxu.dfs.datanode.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 14:08
 */
public class DataNode {

    private volatile Boolean shouldRun;

    /**
     * 负责跟一组nameNode通信的组件
     */
    private NameNodeOfferService offerService;

    public static void main(String[] args) {
        DataNode dataNode = new DataNode();
        dataNode.initialize();
        dataNode.run();
    }

    /**
     * 初始化DataNode
     */
    private void initialize() {
        this.shouldRun = true;
        this.offerService = new NameNodeOfferService();
        this.offerService.start();
    }

    /**
     * 允许DataNode
     */
    private void run() {
        try {
            while (shouldRun) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
