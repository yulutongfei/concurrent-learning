package com.sunxu;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/8/17 22:52
 */
public class CopyOnWriteArrayListDemo {

    public static void main(String[] args) {
        List<String> list = new CopyOnWriteArrayList<>();
        list.add("sunxu");
        list.set(0, "wangss");
        list.remove("sunxu");
        list.add("sunxu1");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String s = iterator.next();
            System.out.println(s);
        }
        System.out.println(list.size());
        System.out.println(list.get(0));
    }
}
