package com.shuyun.datasync.core.template.strategy;

import com.shuyun.datasync.domain.ColumnMapping;
import com.shuyun.datasync.domain.TaskConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jiawei.guo on 2018/8/21.
 */
public class CoverSyncStrategyParallel extends CoverSyncStrategy {
    private static Logger logger = Logger.getLogger(CoverSyncStrategyParallel.class);

    public static void handle(List<String> tables,final TaskConfig tc) {
        ExecutorService es = Executors.newFixedThreadPool(tc.getParallelSize());

        SparkSession spark = createSparkSession(tc);

        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

        Broadcast<TaskConfig> taskConfigBroad = sc.broadcast(tc);

        Map<String, JavaRDD<Row>> rddMap = new HashMap<String, JavaRDD<Row>>();

        for(String table : tables) {
            rddMap.put(table, createHbaseRDD(sc, table, taskConfigBroad));
        }

        StructType schema = createSchema(tc);

        for(String table : tables) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    JavaRDD<Row> dataRDD = rddMap.get(table);

                    coverData(spark, dataRDD, schema, tc, table);

                    spark.close();
                    spark.stop();
                }
            });
        }

        es.shutdown();
    }
}
