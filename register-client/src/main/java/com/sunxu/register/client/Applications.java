package com.sunxu.register.client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/31 15:49
 */
public class Applications {

    private Map<String, Map<String, ServiceInstance>> registry = new HashMap<>();

    public Applications() {
    }

    public Applications(Map<String, Map<String, ServiceInstance>> registry) {
        this.registry = registry;
    }

    public Map<String, Map<String, ServiceInstance>> getRegistry() {
        return registry;
    }

    public void setRegistry(Map<String, Map<String, ServiceInstance>> registry) {
        this.registry = registry;
    }
}
