package com.sunxu.dfs.namenode.server;

import java.util.LinkedList;
import java.util.List;

/**
 * @author 孙许
 * @version 1.0
 * @date 2021/7/20 02:12
 * 负责管理内存中文件目录树
 */
public class FSDirectory {

    /**
     * 内存中的文件目录树
     */
    private INodeDirectory dirTree;

    public FSDirectory() {
        this.dirTree = new INodeDirectory("/");
    }

    /**
     * 创建目录
     * @param path
     */
    public void mkdir(String path) {
        // 先判断下,根目录下有没有"usr"目录存在
        // 如果说有的话,那么再判断一下,"/usr"目录下,有没有"/warehouse"目录存在
        synchronized (dirTree) {
            String[] paths = path.split("/");
            // "","usr","warehouse","hive"
            INodeDirectory parent = dirTree;
            for (String splitedPath : paths) {
                if (splitedPath.trim().equals("")) {
                    continue;
                }
                INodeDirectory dir = findDirectory(parent, splitedPath);
                if (dir != null) {
                    parent = dir;
                    continue;
                }
                // 如果dir为空则创建
                INodeDirectory child = new INodeDirectory(splitedPath);
                parent.addChild(child);
            }
        }
    }

    /**
     * 寻找指定path的INode
     * @param dir /user/warehouse/hive
     * @param path
     * @return
     */
    private INodeDirectory findDirectory(INodeDirectory dir, String path) {
        if (dir.getChildren().size() == 0) {
            // 没有子节点
            return null;
        }
        INodeDirectory resultDir = null;
        for (INode child : dir.getChildren()) {
            if (child instanceof INodeDirectory) {
                // 如果当前节点匹配上就返回
                INodeDirectory childDir = (INodeDirectory) child;
                if (childDir.getPath().equals(path)) {
                    return childDir;
                }
                // 继续在当前节点下寻找
                resultDir = findDirectory(childDir, path);
                if (resultDir != null) {
                    return resultDir;
                }
            }
        }
        return null;
    }

    private interface  INode {

    }

    /**
     * 代表文件目录树中的一个目录
     */
    private class INodeDirectory implements INode {

        private String path;
        private List<INode> children;

        private void addChild(INode iNode) {
            this.children.add(iNode);
        }

        public INodeDirectory(String path) {
            this.path = path;
            this.children = new LinkedList<>();
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<INode> getChildren() {
            return children;
        }

        public void setChildren(List<INode> children) {
            this.children = children;
        }
    }

    /**
     * 代表文件目录树中的一个文件
     */
    private class INodeFile implements INode {

        /**
         * 文件名称
         */
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}
