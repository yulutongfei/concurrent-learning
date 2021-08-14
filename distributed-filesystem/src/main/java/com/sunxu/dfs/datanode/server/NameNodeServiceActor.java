package com.sunxu.dfs.datanode.server;

import java.util.concurrent.CountDownLatch;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 14:13
 * 负责跟一组NameNode中的某一个进行通信的线程组件
 */
public class NameNodeServiceActor {

    /**
     * 向自己负责通信的那个NameNode进行注册
     */
    public void register(CountDownLatch latch) {
        Thread registerThread = new RegisterThread(latch);
        registerThread.start();
    }

    /**
     * 负责注册的线程
     */
    class RegisterThread extends Thread {

        CountDownLatch latch;

        public RegisterThread(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            // 发送rpc接口调用请求到NameNode中进行注册
            System.out.println("发送请求到NameNode进行注册...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
        }
    }
}
