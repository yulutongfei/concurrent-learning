package com.sunxu.dfs.namenode.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/20 02:08
 * NameNode的rpc服务的接口
 */
public class NameNodeRpcServer {

    /**
     * 负责管理元数据的核心组件
     */
    private FSNamesystem namesystem;

    public NameNodeRpcServer(FSNamesystem namesystem) {
        this.namesystem = namesystem;
    }

    /**
     * 创建目录
     * @param path 目录路径
     * @return
     * @throws Exception
     */
    public Boolean mkdir(String path) throws Exception {
        return this.namesystem.mkdir(path);
    }

    /**
     * 启动
     */
    public void start() {
        System.out.println("开始监听指定的rpc server的端口号,来接受这个请求");
    }
}
