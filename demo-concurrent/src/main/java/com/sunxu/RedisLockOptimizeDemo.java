package com.sunxu;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RedisLockOptimizeDemo {

    public static void main(String[] args) throws Exception {
        // 定义 一些变量
        Long goodsSkuId = 1L;
        Long purchaseCount = 50L;
        int stockSegmentSeq = new Random().nextInt(10) + 1;

        InventoryDAO inventoryDAO = new InventoryDAO();
        RLock lock = new RLock("stock_" + goodsSkuId + "_" + stockSegmentSeq);

        lock.lock();

        Long stock = inventoryDAO.getStock(goodsSkuId, stockSegmentSeq);

        // 如果查出来库存是0
        if (stock == 0L) {
            lock.unlock();

            boolean foundOtherStockSegment = false;

            for (int i = 1; i <= 10; i++) {
                if (i == stockSegmentSeq) {
                    continue;
                }

                lock = new RLock("stock_" + goodsSkuId + "_" + i);
                lock.lock();

                stock = inventoryDAO.getStock(goodsSkuId, i);
                if (stock != 0) {
                    stockSegmentSeq = i;
                    foundOtherStockSegment = true;
                    break;
                } else {
                    lock.unlock();
                }
            }

            if (!foundOtherStockSegment) {
                System.out.println("商品库存不足");
                return;
            }
        }

        // 如果库存分段正好大于要购买的数量
        if (stock >= purchaseCount) {
            inventoryDAO.updateStock(goodsSkuId, stockSegmentSeq, stock - purchaseCount);
            lock.unlock();
            return;
        }

        // 代码走到这里，就证明说，当前这个分段的库存小于要购买的数量，合并分段加锁
        Long totalStock = stock;

        Map<RLock, Long> otherLocks = new HashMap<RLock, Long>();

        for (int i = 1; i <= 10; i++) {
            if (i == stockSegmentSeq) {
                continue;
            }

            RLock otherLock = new RLock("stock_" + goodsSkuId + "_" + i);
            otherLock.lock();

            Long otherStock = inventoryDAO.getStock(goodsSkuId, i);
            if (otherStock == 0) {
                otherLock.unlock();
                continue;
            }

            totalStock += otherStock;
            otherLocks.put(otherLock, otherStock);

            if (totalStock >= purchaseCount) {
                break;
            }
        }

        // 尝试所有的其他分段之后还是无法满足购买数量
        if (totalStock < purchaseCount) {
            System.out.println("商品库存不足");
            for (Map.Entry<RLock, Long> otherLockEntry : otherLocks.entrySet()) {
                otherLockEntry.getKey().unlock();
            }
            return;
        }

        // 先将最初加的那个分段库存扣减掉
        Long remainReducingStock = purchaseCount - stock;

        inventoryDAO.updateStock(goodsSkuId, stockSegmentSeq, 0L);
        lock.unlock();

        for (Map.Entry<RLock, Long> otherLockEntry : otherLocks.entrySet()) {
            if (remainReducingStock == 0) {
                break;
            }

            RLock otherLock = otherLockEntry.getKey();
            int otherStockSegmentSeq = Integer.parseInt(otherLock.name.substring(
                    otherLock.name.length() - 1));
            Long otherStock = otherLockEntry.getValue();

            if (otherStock <= remainReducingStock) {
                remainReducingStock -= otherStock;
                inventoryDAO.updateStock(goodsSkuId, otherStockSegmentSeq, 0L);
            } else {
                remainReducingStock = 0L;
                inventoryDAO.updateStock(goodsSkuId, otherStockSegmentSeq,
                        otherStock - remainReducingStock);
            }

            otherLock.unlock();
        }
    }

    static class RLock {

        String name;

        public RLock(String name) {
            this.name = name;
        }

        public void lock() {
            System.out.println("加分布式锁：" + name);
        }

        public void unlock() {
            System.out.println("释放分布式锁：" + name);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RLock other = (RLock) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

    }

    static class InventoryDAO {

        public Long getStock(Long goodsSkuId, Integer stockSegmentSeq) {
            return 1000L;
        }

        public void updateStock(Long goodsSkuId, Integer stockSegmentSeq, Long stock) {
            System.out.println("更新商品库存，goodsSkuId=" + goodsSkuId
                    + "，stockSegmentSeq=" + stockSegmentSeq
                    + "，stock=" + stock);
        }

    }

}
