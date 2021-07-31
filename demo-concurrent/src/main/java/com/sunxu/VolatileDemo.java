package com.sunxu;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/15 00:36
 */
public class VolatileDemo {

    static volatile int flag = 0;

    public static void main(String[] args) {

        new Thread(() -> {
            int localFlag = flag;
            while (true) {
                // 在这个线程里,读到的值一直是旧的值
                if (localFlag != flag) {
                    System.out.println("读取到修改后的标志位: " + flag);
                    localFlag = flag;
                }
            }
        }).start();

        new Thread(() -> {
            int localFlag = flag;
            while (true) {
                System.out.println("标志位被修改为了: " + ++localFlag);
                // 这个flag不断的被修改
                flag = localFlag;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
