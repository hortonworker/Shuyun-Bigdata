package com.shuyun.datasync.utils;

import com.shuyun.datasync.common.AppConfiguration;

import java.sql.SQLException;

/**
 * Created by jiawei.guo on 2018/9/3.
 */
public class HiveUtil {
    static {
        try {
            Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static HiveSession createHiveSession() throws SQLException {
        String url = AppConfiguration.get("hive.jdbc.url");
        String username = AppConfiguration.get("hive.jdbc.username");
        String pwd = AppConfiguration.get("hive.jdbc.pwd");
        return new HiveSession(url, username, pwd);
    }

}
