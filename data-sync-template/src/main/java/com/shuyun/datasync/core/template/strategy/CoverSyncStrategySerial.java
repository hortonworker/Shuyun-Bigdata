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

import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class CoverSyncStrategySerial extends CoverSyncStrategy {
    private static Logger logger = Logger.getLogger(CoverSyncStrategySerial.class);

    public static void handle(List<String> tables,final TaskConfig tc) {
        SparkSession spark = createSparkSession(tc);

        JavaSparkContext sc = getSparkContext(spark, tc);

        Broadcast<TaskConfig> taskConfigBroad = sc.broadcast(tc);

        StructType schema = createSchema(tc);

        for(String table : tables) {
            ZKLock lock = ZKUtil.lock(table);

            JavaRDD<Row> dataRDD = createHbaseRDD(sc, table, taskConfigBroad);

            coverData(spark, dataRDD, schema, tc, table);

            updateTableStatus(table);
            lock.release();
        }
        spark.close();
        spark.stop();
    }


}
