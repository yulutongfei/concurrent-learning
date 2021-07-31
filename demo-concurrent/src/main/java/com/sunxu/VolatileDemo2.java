package com.sunxu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/15 00:36
 */
public class VolatileDemo2 {

    static volatile Map<String, String> flag = new HashMap<>();

    public static void main(String[] args) {

        new Thread(() -> {
            Map<String, String> localFlag = flag;
            while (true) {
                // 在这个线程里,读到的值一直是旧的值
                if (localFlag.size() != flag.size()) {
                    System.out.println("读取到修改后的标志位: " + flag);
                    localFlag = flag;
                }
            }
        }).start();

        new Thread(() -> {
            Map<String, String> localFlag = flag;
            int num = 0;
            while (true) {
                num++;
                localFlag.put(String.valueOf(num), "sunxu");
                System.out.println("标志位数量被修改为了: " + localFlag.size());
                // 这个flag不断的被修改
                flag = localFlag;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
