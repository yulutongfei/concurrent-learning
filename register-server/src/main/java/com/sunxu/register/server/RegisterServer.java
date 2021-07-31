package com.sunxu.register.server;

import java.util.UUID;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/14 00:05
 * 代表了服务注册中心
 */
public class RegisterServer {

    public static void main(String[] args) throws Exception {

        RegisterServerController controller = new RegisterServerController();
        String serviceId = UUID.randomUUID().toString().replace("-", "");

        // 模拟发起一个服务注册的请求
        RegisterRequest request = new RegisterRequest();
        request.setServiceName("inventory-service");
        request.setIp("192.168.31.208");
        request.setHostName("inventory-service-01");
        request.setPort(9000);
        request.setServiceId(serviceId);

        controller.register(request);

        // 模拟心跳请求
        HeartBeatRequest heartBeatRequest = new HeartBeatRequest();
        heartBeatRequest.setServiceId(serviceId);
        heartBeatRequest.setServiceName("inventory-service");
        controller.heartBeat(heartBeatRequest);

        ServiceAliveMonitor monitor = new ServiceAliveMonitor();
        // 一般来说
        monitor.start();
        while (true) {
            Thread.sleep(30 * 1000);
        }
    }
}
