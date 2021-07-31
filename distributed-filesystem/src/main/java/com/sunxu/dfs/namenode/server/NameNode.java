package com.sunxu.dfs.namenode.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/20 02:16
 * NameNode核心启动类
 */
public class NameNode {

    /**
     * NameNode是否在运行
     */
    private volatile Boolean shouldRun;

    /**
     * 负责管理元数据的核心组件
     */
    private FSNamesystem namesystem;

    /**
     * NameNode对外提供rpc接口的server,可以响应请求
     */
    private NameNodeRpcServer rpcServer;

    public NameNode() {
        this.shouldRun = true;
    }

    public static void main(String[] args) {
        String path = "/usr/warehouse/hive";
        System.out.println(path.split("/").length);
        NameNode nameNode = new NameNode();
        nameNode.initialize();
        nameNode.run();
    }

    /**
     * 初始化NameNode
     */
    private void initialize() {
        this.namesystem = new FSNamesystem();
        this.rpcServer = new NameNodeRpcServer(this.namesystem);
        this.rpcServer.start();
    }

    /**
     * 让NameNode运行起来
     */
    private void run() {
        while (shouldRun) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
