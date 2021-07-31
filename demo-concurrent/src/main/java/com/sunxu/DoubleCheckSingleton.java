package com.sunxu;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/16 00:56
 */
public class DoubleCheckSingleton {

    private static volatile DoubleCheckSingleton instance;

    public DoubleCheckSingleton getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckSingleton.class) {
                if (instance == null) {
                    DoubleCheckSingleton.instance = new DoubleCheckSingleton();
                }
            }
        }
        return instance;
    }
}
