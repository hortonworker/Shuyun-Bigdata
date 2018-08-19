package example.storm.phoenix;


import java.util.Properties;
import java.util.UUID;


import org.apache.kafka.common.requests.ListOffsetRequest;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.*;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

public class MainTopology {

    public static void main(String[] args) throws Exception{

        // Zookeeper quorum
        BrokerHosts brokerHosts = new ZkHosts("sandbox-hdp.hortonworks.com:2181");

        /** Creating SpoutConfig Object
         Hosts − The BrokerHosts can be any implementation of BrokerHosts interface

         Topic − topic name.

         zkRoot − ZooKeeper root path.

         id − The spout stores the state of the offsets its consumed in Zookeeper. The id should uniquely identify your spout.
         **/
        SpoutConfig spoutConfig = new SpoutConfig(brokerHosts, "test", "", "kafkaTopology");

        // for storing properties of KafkaBolt
        Config conf = new Config();
        conf.setDebug(true);

        //convert the ByteBuffer to String.
        spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

        // setting for how often to save the current Kafka offset to ZooKeeper
        spoutConfig.stateUpdateIntervalMs = 2000;

        // Retry strategy for failed messages
        spoutConfig.failedMsgRetryManagerClass = ExponentialBackoffMsgRetryManager.class.getName();

        // Exponential back-off retry settings.  These are used by ExponentialBackoffMsgRetryManager for retrying messages after a bolt
        // calls OutputCollector.fail(). These come into effect only if ExponentialBackoffMsgRetryManager is being used.
        // Initial delay between successive retries
        spoutConfig.retryInitialDelayMs = 0;
        spoutConfig.retryDelayMultiplier = 1.0;

        // Maximum delay between successive retries
        spoutConfig.retryDelayMaxMs = 60 * 1000;
        // Failed message will be retried infinitely if retryLimit is less than zero.
        spoutConfig.retryLimit = 2;

        spoutConfig.fetchSizeBytes = 1024 * 1024;
        spoutConfig.socketTimeoutMs = 10000;
        spoutConfig.fetchMaxWait = 10000;
        spoutConfig.bufferSizeBytes = 1024 * 1024;
        spoutConfig.ignoreZkOffsets = false;
        //kafka.api.OffsetRequest.EarliestTime() read from beginning
        spoutConfig.startOffsetTime = ListOffsetRequest.LATEST_TIMESTAMP;
        spoutConfig.maxOffsetBehind = Long.MAX_VALUE;
        spoutConfig.useStartOffsetTimeIfOffsetOutOfRange = true;
        spoutConfig. metricsTimeBucketSizeInSecs = 60;


        TopologyBuilder builder = new TopologyBuilder();
        //Assign SpoutConfig to KafkaSpout.
        builder.setSpout("kafka_spout", new KafkaSpout(spoutConfig));
        //class PhoenixBolt used for testing
        builder.setBolt("phoenix-bolt", new PhoenixBolt2()).shuffleGrouping("kafka_spout");

        String topologyName = "kafkaTopology";
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(topologyName, conf, builder.createTopology());

        Thread.sleep(10000);
//        cluster.killTopology(topologyName);
//        cluster.shutdown();

    }
}
