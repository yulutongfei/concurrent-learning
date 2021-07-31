package com.sunxu.register.client;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/10 16:18
 */
public class Application {

    public static void main(String[] args) throws InterruptedException {
        RegisterClient client = new RegisterClient();
        client.onStart();
        Thread.sleep(35000);
        client.shutdown();
    }
}
