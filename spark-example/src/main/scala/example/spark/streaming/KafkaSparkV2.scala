package example.spark.streaming


import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.TaskContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Durations, StreamingContext}

object KafkaSparkV2 {
  def main(args: Array[String]) {

    //configuration
    val spark = SparkSession
      .builder()
      .appName("Simplest streaming (spark 2.0) from Kafka SSL")
      .enableHiveSupport()
      .getOrCreate()
    val sparkContext = spark.sparkContext
    //create streaming context
    val streamingContext = new StreamingContext(sparkContext, Durations.seconds(3))

    //create DStream from kafka
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "sandbox-hdp.hortonworks.com:6667",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("test", "test1")
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    stream.map(record => (record.key, record.value))

    stream.foreachRDD { rdd =>
      //get offset
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.foreachPartition { iter =>
        val o: OffsetRange = offsetRanges(TaskContext.get.partitionId)
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      }
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }

}
