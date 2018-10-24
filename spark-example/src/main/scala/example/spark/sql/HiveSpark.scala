package example.spark.sql

import java.io.File
import java.sql.Statement

import org.apache.spark.sql.SparkSession

object HiveSpark {
  private val driverName = "org.apache.hive.jdbc.HiveDriver"

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

    spark.conf.set("hive.enforce.bucketing", "false")
    spark.conf.set("hive.enforce.sorting", "false")

    try
      Class.forName(driverName)
    catch {
      case e: ClassNotFoundException =>
        // TODO Auto-generated catch block
        e.printStackTrace()
        System.exit(1)
    }


    import spark.sql

    //    sql("create external table if not exists temporary_user(firstname varchar(64),lastname varchar(64), address string, country varchar(64),city varchar(64),state varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) row format delimited fields terminated by ',' lines terminated by '\\n' stored as textfile location '/user/hive/temporary_user'")

    val sqlDF = sql("select * from temporary_user limit 10")

    if (sqlDF.count() == 0) {
      //data comes from http://hadooptutorial.info/wp-content/uploads/2014/12/UserRecords.txt
      sql("load data local inpath '/tmp/storm/hive-query/UserRecords.txt' into table temporary_user")
    }

    import java.sql.{Connection, DriverManager}
    val con: Connection = DriverManager.getConnection("jdbc:hive2://edge01.bigdata.shuyun.com:2181,kafka01.bigdata.shuyun.com:2181,kafka02.bigdata.shuyun.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2", "hive", "")
    val stmt: Statement = con.createStatement


    stmt.execute("create table if not exists partitioned_user(firstname varchar(64),lastname varchar(64), address string, city varchar(64),post string, phone1 varchar(64),phone2 string,email string,web string) partitioned by (country varchar(64), state varchar(64)) clustered by(email) into 32 buckets stored as orc TBLPROPERTIES('transactional'='true')")


    sql("set hive.exec.dynamic.partition.mode=nonstrict")
    sql("insert into table partitioned_user partition (country, state) select firstname,lastname,address,city,post,phone1,phone2,email,web,country,state from temporary_user")


    stmt.execute("set hive.support.concurrency=true")
    stmt.execute("set hive.enforce.bucketing = true")
    stmt.execute("set hive.exec.dynamic.partition.mode=nonstrict")
    stmt.execute("set hive.txn.manager = org.apache.hadoop.hive.ql.lockmgr.DbTxnManager")

    stmt.execute("delete from partitioned_user where country ='US' and state = 'AK'")

    stmt.close()

  }

}
