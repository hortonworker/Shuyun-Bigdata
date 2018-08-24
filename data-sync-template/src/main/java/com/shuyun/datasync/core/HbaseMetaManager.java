package com.shuyun.datasync.core;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.common.TableSelectType;
import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.HBaseClient;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class HbaseMetaManager {

    private static Logger logger = Logger.getLogger(HbaseMetaManager.class);

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
        HBaseClient client = new HBaseClient();
        try {
            Admin admin = client.getAdmin();

            HTableDescriptor[] tableDescriptor = admin.listTables(Pattern.compile(taskConfig.getTableName()));

            if(tableDescriptor == null || tableDescriptor.length <= 0) {
                logger.error("get hbase tables error!");
                throw new Exception("load hbase table list error!");
            }

            //Table statusTable = client.getTable(TableName.valueOf(AppConfiguration.get("data-update-status")));

            for(HTableDescriptor desc : tableDescriptor) {
                //Result statusResult = statusTable.get(new Get(desc.getTableName().getName()));
                tables.add(desc.getTableName().getNameAsString().trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return tables;
    }

    public static List<String> getAvailableTables(TaskConfig taskConfig) throws Exception {

        return null;
    }
}
