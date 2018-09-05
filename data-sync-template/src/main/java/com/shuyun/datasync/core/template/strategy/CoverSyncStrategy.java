package com.shuyun.datasync.core.template.strategy;

import com.shuyun.datasync.common.AppConfiguration;
import com.shuyun.datasync.common.FileType;
import com.shuyun.datasync.core.HbaseMetaManager;
import com.shuyun.datasync.domain.ColumnMapping;
import com.shuyun.datasync.domain.TaskConfig;
import com.shuyun.datasync.utils.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiawei.guo on 2018/8/23.
 */
public class CoverSyncStrategy {

    private static Logger logger = Logger.getLogger(CoverSyncStrategy.class);

    protected static void handleCover(SparkSession spark, JavaRDD<Row> dataRDD , TaskConfig tc,String table, StructType schema) {
        ZKLock lock = ZKUtil.lock(table);

        try {
            coverData(spark, dataRDD, schema, tc, table);
            updateTableStatus(table);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("cover table[" + table + "] error", e);
        }
        lock.release();
    }

    protected static JavaSparkContext getSparkContext(final SparkSession spark, final TaskConfig tc) {
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        if(MapUtils.isNotEmpty(tc.getSparkConfigProperties())) {
            for(String key : tc.getSparkConfigProperties().keySet()) {
                sc.getConf().set(key, tc.getSparkConfigProperties().get(key));
            }
        }
        return sc;
    }

    protected static SparkSession createSparkSession(final TaskConfig tc) {
        SparkSession.Builder builder = SparkSession
                .builder()
                .appName(tc.getTaskName())
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        if(MapUtils.isNotEmpty(tc.getSparkConfigProperties())) {
            for(String key : tc.getSparkConfigProperties().keySet()) {
                builder.config(key, tc.getSparkConfigProperties().get(key));
            }
        }
        SparkSession spark = builder.enableHiveSupport().getOrCreate();
        if(StringUtils.isNotBlank(tc.getBucketColumn())) {
            spark.sql("set hive.enforce.bucketing=false");
            spark.sql("set hive.enforce.sorting=false");
            spark.sql("set hive.exec.dynamic.partition.mode=nonstrict");
        }
        return spark;
    }

    protected static StructType createSchema(final TaskConfig tc) {
        List<StructField> structFields=new ArrayList<StructField>();
        for(ColumnMapping mapping : tc.getColumnMapping()) {
            structFields.add(DataTypes.createStructField(mapping.getHiveColumn(), mapping.getType(), true));
        }

        return DataTypes.createStructType(structFields);
    }

    protected static void updateData(final SparkSession spark, final JavaRDD<Row> dataRDD, final StructType schema, final TaskConfig tc, final String table) throws Exception {
        executeCreateSQL(tc, table);

        HiveSession session = HiveUtil.createHiveSession();
        session.execute(makeDeleteSQL(dataRDD, tc, table));
        session.close();

        String tmpTableName = table + "_tmp";
        Dataset stuDf = spark.createDataFrame(dataRDD, schema);
        stuDf.printSchema();
        stuDf.createOrReplaceTempView(tmpTableName);

        spark.sql(makeInsertSQL(tc, table, tmpTableName));
    }

    protected static void coverData(final SparkSession spark, final JavaRDD<Row> dataRDD, final StructType schema, final TaskConfig tc, final String table) throws Exception {
        executeCreateSQL(tc, table);

        String tmpTableName = table + "_tmp";
        String insertSQL = makeInsertSQL(tc, table, tmpTableName, true);

        Dataset stuDf = spark.createDataFrame(dataRDD, schema);
        stuDf.printSchema();
        stuDf.createOrReplaceTempView(tmpTableName);

        spark.sql(insertSQL);
    }

    protected static JavaRDD<Row> createHbaseRDD(JavaSparkContext sc, String table, Broadcast<TaskConfig> taskConfigBroad) {
        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set(HConstants.ZOOKEEPER_QUORUM, AppConfiguration.get("hbase.zookeeper.quorum"));
        hbaseConf.set(HConstants.ZOOKEEPER_CLIENT_PORT, AppConfiguration.get("hbase.zookeeper.property.clientPort"));
        hbaseConf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, AppConfiguration.get("zookeeper.znode.parent"));

        hbaseConf.set(TableInputFormat.INPUT_TABLE, table);

        JavaPairRDD<ImmutableBytesWritable, Result> hbaseRDD = sc.newAPIHadoopRDD(hbaseConf,TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

        JavaRDD<Row> dataRDD = hbaseRDD.map(new Function<Tuple2<ImmutableBytesWritable,Result>, Row>() {

            @Override
            public Row call(Tuple2<ImmutableBytesWritable, Result> tuple) throws Exception {
                Result result = tuple._2();
                List<String> values = new ArrayList<String>();
                TaskConfig taskConfig = taskConfigBroad.getValue();
                for(ColumnMapping mapping : taskConfig.getColumnMapping()) {
                    if(StringUtils.isBlank(mapping.getFamily())) {
                        values.add(Bytes.toString(result.getRow()));
                    } else {
                        values.add(Bytes.toString(result.getValue(Bytes.toBytes(mapping.getFamily()), Bytes.toBytes(mapping.getHbaseColumn()))));
                    }
                }
                //这一点可以直接转化为row类型
                return (Row) RowFactory.create(values.toArray());
            }

        });
        return dataRDD;

    }

    protected static void executeCreateSQL(TaskConfig taskConfig, String tableName) throws Exception {
        StringBuffer sb = new StringBuffer("create table if not exists ");
        sb.append(taskConfig.getDatabase()).append(".").append(tableName);
        sb.append("(");
        for(ColumnMapping mapping : taskConfig.getColumnMapping()) {
            sb.append(mapping.getHiveColumn()).append(" ").append(DataTypeConvert.sparkTypeToHiveType(mapping.getType())).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        if(taskConfig.getFileType() != null && taskConfig.getFileType().equals(FileType.TEXTFILE)) {
            sb.append(" ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' LINES TERMINATED BY '\\n' ");
        }
        if(StringUtils.isNotBlank(taskConfig.getBucketColumn())) {
            sb.append(" clustered by(").append(taskConfig.getBucketColumn()).append(") into ")
                    .append(taskConfig.getBucketSize()).append(" buckets ");
        }
        if(taskConfig.getFileType() != null) {
            sb.append(" STORED AS ").append(taskConfig.getFileType());
        }
        if(MapUtils.isNotEmpty(taskConfig.getTblproperties())) {
            sb.append(" tblproperties (");
            for(String key : taskConfig.getTblproperties().keySet()) {
                sb.append("\"").append(key).append("\"=");
                sb.append("\"").append(taskConfig.getTblproperties().get(key)).append("\"");
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");
        }
        String createSQL = sb.toString();
        logger.info(createSQL);
        HiveSession session = HiveUtil.createHiveSession();
        session.execute(createSQL);
        session.close();
    }

    protected static String makeInsertSQL(TaskConfig taskConfig, String tableName, String tmpTableName) {
        return makeInsertSQL(taskConfig, tableName, tmpTableName, false);
    }

    protected static String makeInsertSQL(TaskConfig taskConfig, String tableName, String tmpTableName, boolean overwrite) {
        String mode = "into table";
        if(overwrite)
            mode = "overwrite table";
        StringBuffer sb = new StringBuffer("insert ").append(mode).append(" ");
        sb.append(taskConfig.getDatabase()).append(".").append(tableName);
        sb.append(" select * from ").append(tmpTableName);

        String insertSQL = sb.toString();
        logger.info(insertSQL);
        return insertSQL;
    }

    protected static List<String> makeDeleteSQL(final JavaRDD<Row> dataRDD, TaskConfig taskConfig, String tableName) {
        List<String> sqls = new ArrayList<String>();
        sqls.add("set hive.enforce.bucketing=true");
        sqls.add("set hive.exec.dynamic.partition.mode=nonstrict");
        /*final StringBuffer sb = new StringBuffer("insert overwrite table ");
        sb.append(taskConfig.getDatabase()).append(".").append(tableName);
        sb.append(" select * from ").append(taskConfig.getDatabase()).append(".").append(tableName).append(" where ");
        String primaryKey = null;
        int tmpPrimaryKeyindex = 0;
        for(ColumnMapping mapping : taskConfig.getColumnMapping()) {
            if(mapping.isPrimaryKey()) {
                primaryKey = mapping.getHiveColumn();
                sb.append(primaryKey) .append(" not in ");
                break;
            }
            tmpPrimaryKeyindex ++;
        }
        if(StringUtils.isBlank(primaryKey)) {
            logger.error("no primaryKey from update!");
        }
        sb.append("( ");
        final int tmpIndex = tmpPrimaryKeyindex;
        for(Row row : dataRDD.collect()) {
            Object obj = row.get(tmpIndex);
            //如果后续支持基础类型再进行扩展
            sb.append("'").append(obj.toString()).append("',");
        }

        sb.deleteCharAt(sb.length() - 1);

        sb.append(")");
        String deleteSQL = sb.toString();
        logger.info(deleteSQL);

        sqls.add(deleteSQL);

        return sqls;*/
        final StringBuffer sb = new StringBuffer("delete from ");
        sb.append(taskConfig.getDatabase()).append(".").append(tableName).append(" where ");
        String primaryKey = null;
        int tmpPrimaryKeyindex = 0;
        for(ColumnMapping mapping : taskConfig.getColumnMapping()) {
            if(mapping.isPrimaryKey()) {
                primaryKey = mapping.getHiveColumn();
                sb.append(primaryKey) .append(" in ");
                break;
            }
            tmpPrimaryKeyindex ++;
        }
        if(StringUtils.isBlank(primaryKey)) {
            logger.error("no primaryKey from update!");
        }
        sb.append("( ");
        final int tmpIndex = tmpPrimaryKeyindex;
        for(Row row : dataRDD.collect()) {
            Object obj = row.get(tmpIndex);
            //如果后续支持基础类型再进行扩展
            sb.append("'").append(obj.toString()).append("',");
        }

        sb.deleteCharAt(sb.length() - 1);

        sb.append(")");
        String deleteSQL = sb.toString();
        logger.info(deleteSQL);

        sqls.add(deleteSQL);

        return sqls;
    }

    protected static void updateTableStatus(String tableName) {
        HbaseMetaManager.updateStatus(tableName);
    }
}
