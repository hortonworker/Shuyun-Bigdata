package com.shuyun.datasync.utils;

import com.shuyun.datasync.common.AppConfiguration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;


public class HBaseClient {

    private Configuration HB_CONF = HBaseConfiguration.create();
    private Connection connection;

    public HBaseClient() {
        try {
            HB_CONF.set("hbase.zookeeper.quorum", AppConfiguration.get("hbase.zookeeper.quorum"));
            HB_CONF.setInt("hbase.zookeeper.property.clientPort", Integer.valueOf(AppConfiguration.get("hbase.zookeeper.property.clientPort")));
            HB_CONF.set("zookeeper.znode.parent", AppConfiguration.get("zookeeper.znode.parent"));
            connection = ConnectionFactory.createConnection(HB_CONF);
        } catch (ZooKeeperConnectionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Admin getAdmin() throws IOException {
        return connection.getAdmin();
    }

    public Table getTable(TableName table) throws IOException {
        return connection.getTable(table);
    }

    public void close() throws IOException {
        if (connection == null) {
            return;
        }
        connection.close();
    }


}
