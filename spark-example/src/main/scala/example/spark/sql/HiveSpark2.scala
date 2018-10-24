package example.spark.sql

import java.io.File
import java.sql.Statement

import org.apache.spark.sql.{DataFrame, SparkSession}

object HiveSpark2 {

  def main(args: Array[String]) {
    //warehouseLocation points to the default location for managed database and tables;
    val warehouseLocation = new File("spark-warehouse").getAbsolutePath

    //configuration
    val spark = SparkSession
      .builder()
      .appName("test ability for spark sql (spark 2.3) to read and write hive bucket table")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .config("tez.lib.uris", "/hdp/apps/2.6.5.0-292/tez/tez.tar.gz")
      .enableHiveSupport()
      .getOrCreate()


    import spark.sql

//    sql("set hive.enforce.bucketing=false")
//    sql("set hive.enforce.sorting=false")

    sql("create table if not exists user_orc (firstname varchar(64),lastname varchar(64), address string, country varchar(64),city varchar(64),state varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) clustered by(email) into 32 buckets stored as orc TBLPROPERTIES('transactional'='true')")

    sql("create table if not exists temporary_user(firstname varchar(64),lastname varchar(64), address string, country varchar(64),city varchar(64),state varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) row format delimited fields terminated by ',' lines terminated by '\\n' stored as textfile")

    val count = sql("select * from temporary_user limit 10").count()

    if (count==0) {
      //data comes from http://hadooptutorial.info/wp-content/uploads/2014/12/UserRecords.txt
      sql("load data local inpath '/tmp/storm/hive-query/UserRecords.txt' into table temporary_user")
    }


    sql("create table if not exists partitioned_user(firstname varchar(64),lastname varchar(64), address string, city varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) partitioned by (country varchar(64), state varchar(64)) clustered by(email) into 32 buckets stored as orc TBLPROPERTIES('transactional'='true')")


    sql("set hive.exec.dynamic.partition.mode=nonstrict")
    sql("insert into table partitioned_user partition (country, state) select firstname,lastname,address,city,post,phone1,phone2,email,web,country,state from temporary_user")


    sql("set hive.support.concurrency=true")
    sql("set hive.enforce.bucketing = true")
    sql("set hive.exec.dynamic.partition.mode=nonstrict")
    sql("set hive.txn.manager = org.apache.hadoop.hive.ql.lockmgr.DbTxnManager")

    sql("delete from partitioned_user where country ='US' and state = 'AK'")

  }

}
