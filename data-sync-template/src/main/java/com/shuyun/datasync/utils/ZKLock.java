package com.shuyun.datasync.utils;

import com.shuyun.datasync.common.AppConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiawei.guo on 2018/8/28.
 */
public class ZKLock {
    private static Logger logger = Logger.getLogger(ZKLock.class);

    private InterProcessMutex mutex = null;
    private String tableName;

    public ZKLock(final CuratorFramework client, String tableName) {
        String lockPathInfo = AppConfiguration.get("zookeeper.lock.info");
        mutex = new InterProcessMutex(client, lockPathInfo.replace("#TABLE_NAME#", tableName));
        this.tableName = tableName;
    }

    public void acquire(long time, TimeUnit timeUnit) {
        while (true) {
            boolean flag = false;
            try {
                flag = mutex.acquire(time, timeUnit);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(flag) {
                logger.warn("get lock [" + tableName + "]!");
                break;
            } else {
                logger.warn("wait [" + tableName + "] lock!");
            }
        }
    }

    public void release() {
        try {
            this.mutex.release();
            logger.warn("release lock [" + tableName + "]!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
