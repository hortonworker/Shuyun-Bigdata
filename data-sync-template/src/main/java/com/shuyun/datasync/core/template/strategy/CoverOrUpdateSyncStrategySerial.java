package com.shuyun.datasync.core.template.strategy;

import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.ZKLock;
import com.shuyun.datasync.utils.ZKUtil;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/23.
 */
public class CoverOrUpdateSyncStrategySerial extends CoverOrUpdateSyncStrategy {

    private static Logger logger = Logger.getLogger(CoverOrUpdateSyncStrategySerial.class);

    public static void handle(List<String> tables, final TaskConfig tc) {

        List<String> udpateTables = new ArrayList<String>();
        List<String> coverTables = new ArrayList<String>();

        splitCoverUpdate(tables, udpateTables, coverTables);

        SparkSession spark = createSparkSession(tc);

        JavaSparkContext sc = getSparkContext(spark, tc);

        Broadcast<TaskConfig> taskConfigBroad = sc.broadcast(tc);

        StructType schema = createSchema(tc);

        //覆盖处理
        for(String tableName : coverTables) {

            JavaRDD<Row> dataRDD = createHbaseRDD(sc, tableName, taskConfigBroad);

            handleCover(spark, dataRDD, tc, tableName, schema);
        }

        //增量update
        for(String tableName : udpateTables) {
            JavaRDD<Row> dataRDD = createHbaseRDD(sc, "increment_"+tableName, taskConfigBroad);
            handleUpdate(spark, dataRDD, tc, tableName, schema);
        }
        spark.close();
        spark.stop();

    }

}
