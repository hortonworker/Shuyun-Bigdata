package com.shuyun.datasync.core;

import com.shuyun.datasync.common.SyncStrategyType;
import com.shuyun.datasync.domain.ColumnMapping;
import com.shuyun.datasync.domain.TaskConfig;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class TaskConfigManager {

    /**
     * 加载任务配置信息
     * @param configId
     * @return
     */
    public static TaskConfig loadConfig(String configId) {
        return getTestConfig(configId);
    }

    private static TaskConfig getTestConfig(String configId) {
        TaskConfig config = new TaskConfig();
        config.setId(Long.valueOf(configId));
        config.setTaskName("trade data hbase to hive");
        config.setTableName("trade_.*");
        if(config.getId() == 1) {
            config.setSyncStrategy(SyncStrategyType.SERIAL_COVER_ALWAYS);
        } else {
            config.setSyncStrategy(SyncStrategyType.PARALLEL_COVER_ALWAYS);
            config.setParallelSize(2);
        }
        config.setDatabase("default");
        config.setFileType("ORC");
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
}
