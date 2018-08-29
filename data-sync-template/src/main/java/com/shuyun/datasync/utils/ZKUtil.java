package com.shuyun.datasync.utils;

import com.shuyun.datasync.common.AppConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiawei.guo on 2018/8/27.
 */
public class ZKUtil {
    private static Logger logger = Logger.getLogger(ZKUtil.class);

    private static CuratorFramework client = null;

    public static void init() {
        client = CuratorFrameworkFactory.newClient(
                AppConfiguration.get("zookeeper.quorum"),
                new RetryNTimes(5, 5000)
        );
        client.start();
    }

    public static void stop() {
        client.close();
    }

    public static ZKLock lock(String tableName) {

        ZKLock lock = new ZKLock(client, tableName);
        lock.acquire(10, TimeUnit.SECONDS);
        return lock;
    }

    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                "127.0.0.1:2181",
                new RetryNTimes(5, 5000)
        );
        client.start();

        //可重入
        InterProcessMutex mutex = new InterProcessMutex(client, "/data_plt/hbase_table_status/b_top_trade/locks");
        //不可重入
        //InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(client, "/data_plt/hbase_table_status/b_top_trade");
        while (true) {
            boolean flag = false;
            try {
                flag = mutex.acquire(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(flag) {
                break;
            } else {
                System.out.println("wait lock!");
            }
        }



        for(int i=1;i<100;i++) {
            System.out.println("++++");
            Thread.sleep(1000);
        }

        mutex.release();
    }
}
