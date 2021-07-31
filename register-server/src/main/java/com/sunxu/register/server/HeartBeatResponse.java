package com.sunxu.register.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/10 11:25
 */
public class HeartBeatResponse {

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
