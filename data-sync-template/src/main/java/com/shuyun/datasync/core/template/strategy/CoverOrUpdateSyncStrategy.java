package com.shuyun.datasync.core.template.strategy;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.HBaseClient;
import com.shuyun.datasync.utils.ZKLock;
import com.shuyun.datasync.utils.ZKUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/29.
 */
public class CoverOrUpdateSyncStrategy extends CoverSyncStrategy {

    private static Logger logger = Logger.getLogger(CoverOrUpdateSyncStrategy.class);

    protected static void handleUpdate(SparkSession spark, JavaRDD<Row> dataRDD, TaskConfig tc, String tableName, StructType schema) {
        ZKLock lock = ZKUtil.lock(tableName);
        try {
            updateData(spark, dataRDD, schema, tc, tableName);
            updateTableStatus(tableName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("update [" + tableName + "] table error!", e);
        }

        lock.release();
    }

    protected static void splitCoverUpdate(List<String> tables, List<String> udpateTables, List<String> coverTables) {

        long maxCount = Long.valueOf(AppConfiguration.get("hbase.data.update.max.count"));
        HBaseClient client = null;
        try {
            client = new HBaseClient();
            Table table = client.getTable(TableName.valueOf(AppConfiguration.get("hbase.data.status.table.name")));
            for(String tableName : tables) {

                Get get = new Get(Bytes.toBytes(tableName));
                get.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("hits"));

                table.get(get);

                Result result = table.get(get);
                long currentCount = Bytes.toLong(result.getValue(Bytes.toBytes("f1"), Bytes.toBytes("hits")));
                if(currentCount < maxCount) {
                    udpateTables.add(tableName);
                } else {
                    coverTables.add(tableName);
                }
            }
        } catch (Exception e) {
            logger.error("CoverOrUpdateSyncStrategySerial error", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
