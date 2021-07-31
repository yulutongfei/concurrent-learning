package com.sunxu.dfs.namenode.server;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/20 02:11
 * 负责管理元数据的核心组件
 */
public class FSNamesystem {

    /**
     * 负责管理内存文件目录树的组件
     */
    private FSDirectory directory;
    /**
     * 负责管理edits log写入磁盘的组件
     */
    private FSEditlog editlog;

    public FSNamesystem() {
        this.directory = new FSDirectory();
        this.editlog = new FSEditlog();
    }

    /**
     * 创建目录
     * @param path
     * @return
     * @throws Exception
     */
    public Boolean mkdir(String path) throws Exception {
        this.directory.mkdir(path);
        this.editlog.logEdit("创建了一个目录: " + path);
        return true;
    }
}
