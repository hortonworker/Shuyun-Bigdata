package com.shuyun.datasync.core.template.strategy;

import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.ZKLock;
import com.shuyun.datasync.utils.ZKUtil;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jiawei.guo on 2018/8/23.
 */
public class CoverOrUpdateSyncStrategyParallel extends CoverOrUpdateSyncStrategy {

    private static Logger logger = Logger.getLogger(CoverOrUpdateSyncStrategyParallel.class);

    public static void handle(List<String> tables, final TaskConfig tc) {

        List<String> udpateTables = new ArrayList<String>();
        List<String> coverTables = new ArrayList<String>();

        splitCoverUpdate(tables, udpateTables, coverTables);

        ExecutorService es = Executors.newFixedThreadPool(tc.getParallelSize());

        SparkSession spark = createSparkSession(tc);

        JavaSparkContext sc = getSparkContext(spark, tc);

        Broadcast<TaskConfig> taskConfigBroad = sc.broadcast(tc);

        Map<String, JavaRDD<Row>> rddMap = new HashMap<String, JavaRDD<Row>>();

        for(String table : tables) {
            rddMap.put(table, createHbaseRDD(sc, table, taskConfigBroad));
        }

        StructType schema = createSchema(tc);

        //覆盖处理
        for(String tableName : coverTables) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    ZKLock lock = ZKUtil.lock(tableName);
                    JavaRDD<Row> dataRDD = rddMap.get(tableName);

                    coverData(spark, dataRDD, schema, tc, tableName);
                    updateTableStatus(tableName);
                    lock.release();
                }
            });
        }

        //增量update
        for(String tableName : udpateTables) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    ZKLock lock = ZKUtil.lock(tableName);
                    JavaRDD<Row> dataRDD = rddMap.get(tableName);

                    updateData(spark, dataRDD, schema, tc, tableName);
                    updateTableStatus(tableName);
                    lock.release();
                }
            });
        }

        es.shutdown();
        spark.close();
        spark.stop();

    }

}
