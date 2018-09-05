package com.shuyun.datasync.core;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.common.FileType;
import com.shuyun.datasync.common.SyncStrategyType;
import com.shuyun.datasync.common.TableSelectType;
import com.shuyun.datasync.domain.ColumnMapping;
import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.HBaseClient;
import com.shuyun.datasync.utils.JsonUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class TaskConfigManager {

    private static Logger logger = Logger.getLogger(TaskConfigManager.class);

    /**
     * 加载任务配置信息
     * @param configId
     * @return
     */
    public static TaskConfig getConfig(String configId, String runMode) {
        if("mock".equals(runMode)) {
            return getTestConfig(configId);
        }
        HBaseClient client = null;
        try {
            client = new HBaseClient();
            Table table = client.getTable(TableName.valueOf(AppConfiguration.get("config.table.name")));
            Result result = table.get(new Get(Bytes.toBytes(configId)));
            byte[] value = result.getValue(Bytes.toBytes(AppConfiguration.get("config.table.cf")), Bytes.toBytes(AppConfiguration.get("config.table.cn")));
            if(value != null) {
                return JsonUtil.fromJson(Bytes.toString(value), TaskConfig.class);
            }
        } catch (RuntimeException e) {
            logger.error("hbase connection create error!", e);
        } catch (IOException e) {
            logger.error("get config data from hbase error!", e);
        } finally {
            if(client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private static TaskConfig getTestConfig(String configId) {
        TaskConfig config = new TaskConfig();
        config.setId(Long.valueOf(configId));
        config.setTaskName("trade data hbase to hive");
        config.setTableName("trade_.*");
        if(config.getId() == 1) {
            config.setSyncStrategy(SyncStrategyType.SERIAL_COVER_ALWAYS);
        } else if(config.getId() == 2) {
            config.setSyncStrategy(SyncStrategyType.PARALLEL_COVER_ALWAYS);
            config.setParallelSize(2);
        } else if(config.getId() == 3) {
            config.setSyncStrategy(SyncStrategyType.SERIAL_COVER_OR_UPDATE_BY_COUNT);
        } else {
            config.setSyncStrategy(SyncStrategyType.PARALLEL_COVER_OR_UPDATE_BY_COUNT);
            config.setParallelSize(2);
        }
        config.setDatabase("default");
        config.setFileType(FileType.ORC);
        config.setTableSelectType(TableSelectType.ALL);
        //config.setBucketColumn("name");
        //config.setBucketSize(2);

        List<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
        ColumnMapping m1 = new ColumnMapping();
        m1.setFamily("f1");
        m1.setHbaseColumn("id");
        m1.setHiveColumn("id");
        m1.setType(DataTypes.StringType);
        columnMappings.add(m1);

        ColumnMapping m2 = new ColumnMapping();
        m2.setFamily("f1");
        m2.setHbaseColumn("name");
        m2.setHiveColumn("name");
        m2.setType(DataTypes.StringType);
        columnMappings.add(m2);

        config.setColumnMapping(columnMappings);
        return config;
    }

    public static void main(String[] args) {
        TaskConfig tc = getTestConfig("1");
        String json = JsonUtil.toJson(tc);

        System.out.println(json);

        TaskConfig r = JsonUtil.fromJson(json, TaskConfig.class);
        System.out.println(tc.getId());
    }
}
