package com.shuyun.datasync.core;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.HBaseClient;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class HbaseMetaManager {

    private static Logger logger = Logger.getLogger(HbaseMetaManager.class);

    public static void updateStatus(String tableName) {
        HBaseClient client = null;
        try {
            client = new HBaseClient();
            Table table =  client.getTable(TableName.valueOf(AppConfiguration.get("hbase.data.status.table.name")));
            Put put = new Put(Bytes.toBytes(tableName));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("status"), Bytes.toBytes("0"));
            put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("hits"), Bytes.toBytes(0));
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getTables(TaskConfig taskConfig) throws Exception {
        switch (taskConfig.getTableSelectType()) {
            case ALL:
                return getSubTables(taskConfig);
            case AVAILABLE:
                return getAvailableTables(taskConfig);
            default:
                return null;
        }
    }

    public static List<String> getSubTables(TaskConfig taskConfig) throws Exception {
        List<String> tables = new ArrayList<String>();
        HBaseClient client = null;
        try {
            client = new HBaseClient();

            tables.addAll(getAllTables(client, taskConfig.getTableName()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return tables;
    }

    public static List<String> getAvailableTables(TaskConfig taskConfig) throws Exception {
        List<String> availableTables = new ArrayList<String>();
        HBaseClient client = null;
        try {
            client = new HBaseClient();
            List<String> tableList = getAllTables(client, taskConfig.getTableName());

            Table table =  client.getTable(TableName.valueOf(AppConfiguration.get("hbase.data.status.table.name")));
            for(String tableName : tableList) {
                Result r = table.get(new Get(Bytes.toBytes(tableName)));
                if(r != null && Integer.valueOf(Bytes.toString(r.getValue(Bytes.toBytes("f1"), Bytes.toBytes("status")))) == 1) {
                    availableTables.add(tableName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return availableTables;
    }

    private static List<String> getAllTables(HBaseClient client, String tableNameReg) throws Exception {
        List<String> tables = new ArrayList<String>();
        Admin admin = client.getAdmin();

        HTableDescriptor[] tableDescriptor = admin.listTables(Pattern.compile(tableNameReg));

        if(tableDescriptor == null || tableDescriptor.length <= 0) {
            logger.error("get hbase tables error!");
            throw new Exception("load hbase table list error!");
        }

        for(HTableDescriptor desc : tableDescriptor) {
            tables.add(desc.getTableName().getNameAsString().trim());
        }

        return tables;
    }
}
