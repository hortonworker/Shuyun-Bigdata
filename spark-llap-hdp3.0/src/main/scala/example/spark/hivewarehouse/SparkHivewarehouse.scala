package example.spark.hivewarehouse


import java.io.File

import com.hortonworks.spark.sql.hive.llap.HiveWarehouseBuilder
import org.apache.spark.sql.SparkSession

object SparkHivewarehouse {

  def main(args: Array[String]) {
    //warehouseLocation points to the default location for managed database and tables;
    val warehouseLocation = new File("spark-warehouse").getAbsolutePath

    //configuration
    val spark = SparkSession
      .builder()
      .appName("test ability for spark sql (spark 2.3) to read and write hive bucket table")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .config("tez.lib.uris", "/hdp/apps/3.0.0.0-1634/tez/tez.tar.gz")
      .enableHiveSupport()
      .getOrCreate()

    val hive = HiveWarehouseBuilder.session(spark).build()

    hive.setDatabase("default")

    import spark.sql

    sql("create external table if not exists temporary_user(firstname varchar(64),lastname varchar(64), address string, country varchar(64),city varchar(64),state varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) row format delimited fields terminated by ',' lines terminated by '\\n' stored as textfile location '/user/hive/temporary_user'")

    val sqlDF = sql("select * from temporary_user limit 10")

    if (sqlDF.count() == 0) {
      //data comes from http://hadooptutorial.info/wp-content/uploads/2014/12/UserRecords.txt
      sql("load data local inpath '/tmp/hive-query/UserRecords.txt' into table temporary_user")
    }


    hive.createTable("partitioned_user").ifNotExists().column("firstname", "varchar(64)").column("lastname", "varchar(64)").column("address", "string").column("city", "varchar(64)").column("post", "string").column("phone1", "varchar(64)").column("phone2", "string").column("email", "string").column("web", "string").partition("country", "varchar(64)").partition("state", "varchar(64)").create()


    hive.executeUpdate("set hive.exec.dynamic.partition.mode=nonstrict; insert into table partitioned_user partition (country, state) select firstname,lastname,address,city,post,phone1,phone2,email,web,country,state from temporary_user")



    //    sql("set hive.support.concurrency=true")
//    sql("set hive.enforce.bucketing = true")
//    sql("set hive.exec.dynamic.partition.mode=nonstrict")
//    sql("set hive.txn.manager = org.apache.hadoop.hive.ql.lockmgr.DbTxnManager")
//
//    sql("delete from partitioned_user where country ='US' and state = 'AK'")

  }

}