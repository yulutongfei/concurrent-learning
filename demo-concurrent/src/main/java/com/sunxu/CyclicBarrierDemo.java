package com.sunxu;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/14 14:38
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("所有线程完成自己的工作");
        });

        new Thread(() -> {
            System.out.println("线程1完成自己的工作");
            try {
                barrier.await();
                System.out.println("最终结果合并完成，线程1可以退出");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            System.out.println("线程2完成自己的工作");
            try {
                barrier.await();
                System.out.println("最终结果合并完成，线程2可以退出");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();


        new Thread(() -> {
            System.out.println("线程3完成自己的工作");
            try {
                barrier.await();
                System.out.println("最终结果合并完成，线程3可以退出");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println("main线程执行完毕");
    }
}
