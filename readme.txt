
This is supposed you are in a HDP sandbox environment


step 1. create phoenix table in your hbase environment

//connect to phoenix querying server
[root@sandbox-hdp bin]# pwd
/usr/hdp/current/phoenix-client/bin

//enter in phoenix querying terminal
[root@sandbox-hdp bin]# ./sqlline.py localhost:2181

//create new table
0: jdbc:phoenix:localhost:2181> CREATE TABLE IF NOT EXISTS ORDER_HISTORY (
salt CHAR(2) NOT NULL,
orderid CHAR(36) NOT NULL,
account CHAR(36),
vendorid CHAR(36),
itemid CHAR(36),
itemquantity Integer,
status VARCHAR,
time BIGINT,
CONSTRAINT pk PRIMARY KEY (salt, orderid))
DATA_BLOCK_ENCODING='PREFIX',COMPRESSION='gz'
SPLIT ON ('1','2','3');


step 2. create kafka topic

//create topic 'test'
[root@sandbox-hdp bin]# pwd
/usr/hdp/current/kafka-broker/bin

[root@sandbox-hdp bin]# ./kafka-topics.sh --create --topic test --replication-factor 1 --partitions 3 --zookeeper sandbox-hdp.hortonworks.com:2181
Created topic "test".

(optional) step 3. launch kafka producer emulator that keep generate and emit json string, if you get your own streaming source can you ignore this step

//emulate emitting json string
[root@sandbox-hdp bin]# java -jar /tmp/storm/kafka-example-1.0-SNAPSHOT-jar-with-dependencies.jar produce string test sandbox-hdp.hortonworks.com:6667

step 4. compile the storm app

//compile
sh-3.2# cd /Users/shuyun/workspace/shuyun.bigdata/
sh-3.2# ls
.DS_Store		.git			.idea			pom.xml			readme.txt		storm-phoenix-example
sh-3.2# mvn -pl ./storm-phoenix-example/ -am -DskipTests package

//upload to sandbox server
sh-3.2# scp -P 2222 ./target/*.jar root@localhost:/tmp/storm

step 5. launch the storm

//launch storm application
[root@sandbox-hdp bin]# storm jar /tmp/storm/storm-phoenix-example-1.0-SNAPSHOT.jar example.storm.phoenix.MainTopology --jars "/usr/hdp/current/storm-client/lib/storm-core-1.1.0.2.6.5.0-292.jar,/usr/hdp/current/storm-client/external/storm-kafka/storm-kafka-1.1.0.2.6.5.0-292.jar,/usr/hdp/current/kafka-broker/libs/kafka_2.11-1.0.0.2.6.5.0-292.jar,/usr/hdp/current/kafka-broker/libs/scala-library-2.11.12.jar,/usr/hdp/2.6.5.0-292/kafka/libs/kafka-clients-1.0.0.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/hadoop/lib/commons-lang-2.6.jar,/usr/hdp/2.6.5.0-292/storm/contrib/storm-autocreds/hadoop-client-2.7.3.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/hadoop/client/hadoop-hdfs-2.7.3.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/hadoop/client/hadoop-common-2.7.3.2.6.5.0-292.jar,/usr/hdp/2.6.5.0-292/hbase/lib/hbase-client-1.1.2.2.6.5.0-292.jar,/usr/lib/ambari-infra-solr-client/libs/curator-client-2.12.0.jar,/usr/hdp/2.6.5.0-292/hadoop/client/guava-11.0.2.jar,/usr/hdp/2.6.5.0-292/oozie/libserver/json-simple-1.1.jar,/usr/hdp/2.6.5.0-292/kafka/libs/metrics-core-2.2.0.jar,/usr/hdp/current/hbase-client/lib/hbase-common-1.1.2.2.6.5.0-292.jar,/usr/hdp/current/phoenix-client/lib/phoenix-core-4.7.0.2.6.5.0-292.jar,/usr/hdp/current/hbase-client/lib/hbase-protocol-1.1.2.2.6.5.0-292.jar,/usr/hdp/current/hbase-client/lib/htrace-core-3.1.0-incubating.jar,/usr/hdp/2.6.5.0-292/hbase/lib/netty-all-4.0.52.Final.jar"
