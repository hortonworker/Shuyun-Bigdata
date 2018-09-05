The spark-example show how to use mixture of spark2 sql and hive client to create and update hive managed bucketed-partitioned-transaction table


1. log in master01 node in QA cluster


2. set system environment variable by typing command

   set SPARK_MAJOR_VERSION=2

   this is for pointing out which spark you are gonna use


3. create /tmp/storm/hive-query in nodes master01, slave01, slave02, slave03 by command

   mkdir -p /tmp/storm/hive-query

   and then enter the new created directory, download the txt file UserRecords.txt by typing

   cd /tmp/storm/hive-query
   wget http://hadooptutorial.info/wp-content/uploads/2014/12/UserRecords.txt

   this is because spark sql will create temporary_user table and load local file into the table,
   spark runs on yarn, so we place the txt file on all the possible nodemanagers


4. launch the program by command

   spark-submit --master=yarn --deploy-mode=cluster \
   --jars /usr/hdp/2.6.5.0-292/spark2/jars/spark-streaming_2.11-2.3.0.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/oozie/share/lib/spark/spark-streaming-kafka_2.10-1.6.3.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/spark2/jars/spark-sql_2.11-2.3.0.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/spark2/jars/spark-hive_2.11-2.3.0.2.6.5.0-292.jar,/usr/hdp/current/hive-server2-hive2/lib/hive-jdbc-2.1.0.2.6.5.0-292.jar,/usr/hdp/current/tez-client/tez-mapreduce-0.7.0.2.6.5.0-292.jar,/usr/hdp/current/tez-client/tez-api-0.7.0.2.6.5.0-292.jar   \
   --class example.spark.sql.HiveSpark /data/spark-example-1.0-SNAPSHOT.jar


note: the spark-example program already includes core-site.xml, hdfs-site.xml, hive-site.xml files aligned with the QA environment
      and the jdbc url is copied from ambari hive service portal

      if you wanna check the results, better to log in beeline as hive user by typing

      [root@master01 ~]# beeline
      Beeline version 1.2.1000.2.6.5.0-292 by Apache Hive
      beeline> !connect jdbc:hive2://edge01.bigdata.shuyun.com:2181,kafka01.bigdata.shuyun.com:2181,kafka02.bigdata.shuyun.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2
      Connecting to jdbc:hive2://edge01.bigdata.shuyun.com:2181,kafka01.bigdata.shuyun.com:2181,kafka02.bigdata.shuyun.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2
      Enter username for jdbc:hive2://edge01.bigdata.shuyun.com:2181,kafka01.bigdata.shuyun.com:2181,kafka02.bigdata.shuyun.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2: hive
      Enter password for jdbc:hive2://edge01.bigdata.shuyun.com:2181,kafka01.bigdata.shuyun.com:2181,kafka02.bigdata.shuyun.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2:
      Connected to: Apache Hive (version 1.2.1000.2.6.5.0-292)
      Driver: Hive JDBC (version 1.2.1000.2.6.5.0-292)
      Transaction isolation: TRANSACTION_REPEATABLE_READ
      0: jdbc:hive2://edge01.bigdata.shuyun.com:218>