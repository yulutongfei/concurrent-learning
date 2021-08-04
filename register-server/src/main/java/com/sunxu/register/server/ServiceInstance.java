package com.sunxu.register.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/13 23:27
 * 代表一个服务实例
 */
public class ServiceInstance {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 主机名
     */
    private String hostName;

    private int port;

    /**
     * 服务实例id
     */
    private String serviceInstanceId;

    /**
     * 契约
     */
    private Lease lease;

    public ServiceInstance() {
        this.lease = new Lease();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public Lease getLease() {
        return lease;
    }

    public void setLease(Lease lease) {
        this.lease = lease;
    }

    public void renew() {
        if (this.lease != null) {
            this.lease.renew();
        }
    }

    /**
     * 判断当前服务实例契约是否存活
     * @return
     */
    public Boolean isAlive() {
        return this.lease.isAlive();
    }

    private class Lease {

        /**
         * 最近一次心跳的时间
         */
        private volatile Long latestHeartbeatTime = System.currentTimeMillis();

        /**
         * 续约,发送一个心跳
         */
        public void renew() {
            this.latestHeartbeatTime = System.currentTimeMillis();
            System.out.println("服务实例[" + serviceInstanceId + "]，进行续约：" + latestHeartbeatTime);
        }

        public Boolean isAlive() {
            if (System.currentTimeMillis() - latestHeartbeatTime > 90 * 1000) {
                System.out.println("服务实例[" + serviceInstanceId + "],不再存活");
                return false;
            }
            System.out.println("服务实例[" + serviceInstanceId + "],保持存活");
            return true;
        }

        @Override
        public String toString() {
            return "Lease{" +
                    "latestHeartbeatTime=" + latestHeartbeatTime +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "ServiceInstance{" +
                "serviceName='" + serviceName + '\'' +
                ", ip='" + ip + '\'' +
                ", hostName='" + hostName + '\'' +
                ", port=" + port +
                ", serviceInstanceId='" + serviceInstanceId + '\'' +
                ", lease=" + lease +
                '}';
    }
}
