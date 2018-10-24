package example.spark.streaming;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by chen.yang on 10/10/2017.
 */


public final class Kafak2Spark {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws InterruptedException {

        // Create context with a 2 seconds batch interval
        SparkConf sparkConf = new SparkConf().setAppName("Spark2BenchMark");
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(3));

        Collection<String> topicsSet = Arrays.asList("chen");

        HashMap<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("bootstrap.servers", "cheny0.field.hortonworks.com:6668");
        kafkaParams.put("group.id", "test_sparkstreaming_kafka");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("security.protocol", "SASL_SSL");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("ssl.keystore.location", "/temp/ssl/cheny0.field.hortonworks.com.jks");
        kafkaParams.put("ssl.keystore.password", "password");
        kafkaParams.put("ssl.key.password", "password");
        kafkaParams.put("ssl.truststore.location", "/temp/ssl/cheny0.field.hortonworks.com_truststore.jks");
        kafkaParams.put("ssl.truststore.password", "password");

//      System.setProperty( "java.security.krb5.conf", "/etc/krb5.conf" );


        final JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topicsSet, kafkaParams)
                );

        stream.foreachRDD(rdd -> {
            OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
            rdd.foreachPartition(linePar -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];

                ArrayList<String> list = new ArrayList<String>();
                while (linePar.hasNext()) {
                    list.addAll(Arrays.asList(linePar.next().toString().split(" ")));
                }

                System.out.println("partition: " + o.topicPartition() + " " + list.toString());
            });
        });


        // Start the computation
        jssc.start();
        jssc.awaitTermination();
    }
}
