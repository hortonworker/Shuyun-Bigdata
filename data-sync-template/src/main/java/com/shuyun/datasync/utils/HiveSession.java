package com.shuyun.datasync.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/9/3.
 */
public class HiveSession {

    private Connection con;

    public HiveSession(String url, String username, String pwd) throws SQLException {
        con = DriverManager.getConnection(
                url, username, pwd);
    }

    public void close() {
        if(con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute(String sql) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute(sql);
    }

    public void execute(List<String> sqls) throws SQLException {
        for(String sql : sqls) {
            execute(sql);
        }
    }

}
