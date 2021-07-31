package com.sunxu.dfs.namenode.server;

import java.util.LinkedList;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/20 02:12
 * 负责管理edits log日志的核心组件
 */
public class FSEditlog {

    /**
     * 当前递增的txid的序号
     */
    private long txidSeq = 0L;

    /**
     * 内存双缓冲区
     */
    private DoubleBuffer editLogBuffer = new DoubleBuffer();

    /**
     * 当前是否将数据刷入磁盘中
     */
    private volatile Boolean isSyncRunning = false;

    /**
     * 当前是否有线程在等待刷新到磁盘中去
     */
    private volatile Boolean isWaitSync = false;

    /**
     * 在同步到磁盘中最大txid
     */
    private volatile Long syncMaxTxid = 0L;

    /**
     * 每个线程自己本地txid的副本
     */
    private ThreadLocal<Long> localTxid = new ThreadLocal<>();

    /**
     * 记录edit log日志
     *
     * @param content
     */
    public void logEdit(String content) {
        // 这里必须直接加锁(该对象实例就1个)
        synchronized (this) {
            // 获取全局唯一递增的txid,代表了edit log的序号
            txidSeq++;
            long txid = txidSeq;
            localTxid.set(txid);
            // 构造一条edits log
            EditLog editLog = new EditLog(txid, content);
            // 将edits log写入内存缓冲区中,不是直接刷入磁盘文件
            editLogBuffer.write(editLog);
        }

        logSync();
    }

    /**
     * 将内存缓冲中的数据刷入磁盘文件中
     * 在这里尝试允许某一个线程以此将内存缓冲中的数据刷入磁盘文件中
     * 相当于实现一个批量将内存缓冲数据刷磁盘的过程
     */
    private void logSync() {
        // 再次尝试加锁
        synchronized (this) {
            // 如果当前正好有人在刷内存到磁盘中
            if (isSyncRunning) {
                // 加入说某个线程已经把txid = 1,2,3,4,5的edits log都从syncBuffer刷入磁盘了
                // 或者说此时正在刷入磁盘中
                // 那么这个时候来一个线程,他对应txid=3,此时他是可以直接返回了
                long txid = localTxid.get();
                if (txid <= syncMaxTxid) {
                    return;
                }

                // 假如此时来一个txid = 6的线程
                // 他就需要做一些等待,同时要释放锁
                if (isWaitSync) {
                    return;
                }
                // 只会又一个线程在等待
                isWaitSync = true;
                while (isSyncRunning) {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isWaitSync = false;
            }
            // 交换2块缓冲区
            editLogBuffer.setReadyToSync();
            // 然后可以保存一下当前要同步到磁盘中去的最大的txid
            // 此时editLogBuffer中的syncBuffer这块区域,交换完以后这里可能有多条数据
            // 而且他里面的edits log的txid一定是从小到大的
            syncMaxTxid = editLogBuffer.getMaxTxid();
            // 设置当前正在同步标志位
            isSyncRunning = true;
        }

        // 开始同步内存缓冲的数据到磁盘文件里去
        // 这个过程其实比较慢,基本肯定是毫秒级
        editLogBuffer.flush();

        synchronized (this) {
            // 同步完了磁盘之后就会将标志位复位,再释放锁
            isSyncRunning = false;
            // 唤醒可能正在等待他同步完磁盘的线程
            notifyAll();
        }
    }

    /**
     * 代表一条edits log
     */
    class EditLog {

        Long txid;
        String content;

        public EditLog(Long txid, String content) {
            this.txid = txid;
            this.content = content;
        }
    }


    /**
     * 内存双缓冲
     */
    class DoubleBuffer {

        /**
         * 是专门用来承载线程写入edits log
         */
        LinkedList<EditLog> currentBuffer = new LinkedList<>();
        /**
         * 专门用来将数据同步到磁盘中的一块缓冲
         */
        LinkedList<EditLog> syncBuffer = new LinkedList<>();

        /**
         * 将edit log写到内存缓冲里去
         *
         * @param log
         */
        public void write(EditLog log) {
            currentBuffer.add(log);
        }

        /**
         * 交换2块缓冲区,为了同步内存数据到磁盘做准备
         */
        public void setReadyToSync() {
            LinkedList<EditLog> tmp = currentBuffer;
            currentBuffer = syncBuffer;
            syncBuffer = tmp;
        }

        /**
         * 获取sync buffer缓冲区里的最大的一块缓冲
         *
         * @return
         */
        public Long getMaxTxid() {
            return syncBuffer.getLast().txid;
        }

        /**
         * 将syncBuffer缓冲区的数据刷入磁盘中
         */
        public void flush() {
            for (EditLog editLog : syncBuffer) {
                System.out.println("将edit log写入磁盘文件中: " + editLog);
            }
            syncBuffer.clear();
        }
    }

}
