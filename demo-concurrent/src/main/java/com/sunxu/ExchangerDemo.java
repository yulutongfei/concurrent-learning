package com.sunxu;

import java.util.concurrent.Exchanger;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 23:17
 */
public class ExchangerDemo {

    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                String data = exchanger.exchange("线程1的数据");
                System.out.println("线程1获取到线程2交换过来的数据：" + data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                String data = exchanger.exchange("线程2的数据");
                System.out.println("线程2获取到线程1交换过来的数据：" + data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
