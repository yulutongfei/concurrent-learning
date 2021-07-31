package com.sunxu.register.client;

import java.util.UUID;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/10 11:10
 */
public class RegisterClient {

    /**
     * http通信组建
     */
    private final HttpSender httpSender;
    /**
     * 服务id
     */
    private final String serviceInstanceId;

    private final Thread heartBeatWorker;

    /**
     * 客户端缓存的注册表
     */
    private final CachedServiceRegistry registry;

    /**
     * 服务实例是否运行
     */
    private volatile Boolean isRunning;

    public RegisterClient() {
        this.httpSender = new HttpSender();
        this.serviceInstanceId = UUID.randomUUID().toString().replace("-", "");
        this.heartBeatWorker = new HeartBeatWorker();
        this.registry = new CachedServiceRegistry(this, httpSender);
        this.isRunning = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 启动组件
     */
    public void onStart() {
        Thread registerThread = new Thread(new RegisterWorker());
        // 启动注册线程
        registerThread.start();
        try {
            // 注册完毕
            registerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 启动心跳线程,定时发送心跳信息
        this.heartBeatWorker.start();
        // 初始化客户端缓存注册表
        this.registry.initialize();
    }

    /**
     * 终止组件
     */
    public void shutdown() {
        this.isRunning = false;
        this.heartBeatWorker.interrupt();
        this.registry.destroy();
        this.httpSender.cancel("eshop-service", serviceInstanceId);
    }


    /**
     * 心跳
     */
    private class HeartBeatWorker extends Thread {


        @Override
        public void run() {
            HeartBeatRequest heartBeatRequest = new HeartBeatRequest();
            heartBeatRequest.setServiceId(serviceInstanceId);
            HeartBeatResponse heartBeatResponse = null;

            while (isRunning) {
                try {
                    heartBeatResponse = httpSender.heartBeat(heartBeatRequest);
                    System.out.println("心跳的结果为：" + heartBeatResponse.getStatus() + "......");
                    Thread.sleep(30 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册
     */
    private class RegisterWorker implements Runnable {

        @Override
        public void run() {
            RegisterRequest request = new RegisterRequest();
            request.setHostName("eshop-cache");
            request.setPort(9000);
            request.setServiceId(serviceInstanceId);
            request.setIp("172.22.32.3");
            request.setServiceName("eshop-service");
            RegisterResponse registerResponse = httpSender.register(request);
            System.out.println("服务注册的结果是：" + registerResponse.getStatus() + "......");
        }
    }
}
