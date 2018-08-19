package example.storm.phoenix;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;


public class PhoenixBolt2 extends BaseRichBolt {

    private static final Log logger = LogFactory.getLog(PhoenixBolt2.class);
    private OutputCollector collector;
    private ObjectMapper mapper;

    @Override
    public void execute(Tuple tuple) {
        long ttt = System.currentTimeMillis();
        String sentence = tuple.getString(0);

        JsonMessage msg = null;
        try {
            msg = mapper.readValue(sentence, JsonMessage.class);
            byte[] rowkey =
                    Bytes.toBytes(StringUtils.rightPad(Utils.makeSalt(msg.getOrderID()) + msg.getOrderID(), 38));
            Put put = new Put(rowkey);
            long ctime = msg.getTime();
            //	addColumn(byte[] family, byte[] qualifier, long ts, byte[] value)
            put.addColumn("0".getBytes(), "account".getBytes(), ctime, msg.getAccount().getBytes());
            put.addColumn("0".getBytes(), "vendorid".getBytes(), ctime, msg.getVendorID().getBytes());
            put.addColumn("0".getBytes(), "itemid".getBytes(), ctime, msg.getItemID().getBytes());
            put.addColumn("0".getBytes(), "itemquantity".getBytes(), ctime, Utils.encode(msg.getItemQuantity()));
            put.addColumn("0".getBytes(), "status".getBytes(), ctime, msg.getStatus().getBytes());
            put.addColumn("0".getBytes(), "time".getBytes(), ctime, Utils.encode(msg.getTime()));

            List<Put> puts = new ArrayList<>();
            puts.add(put);

            try {
                if (!puts.isEmpty()) {
                    Table table = HBaseClient.INSTANCE.getTable(TableName.valueOf("ORDER_HISTORY"));
                    table.put(puts);
                }
            } catch (IOException e) {
                collector.fail(tuple);
                logger.error("fails to upsert in table ORDER_HISTORY");
                throw new RuntimeException(e);
            }

         } catch (IOException e) {
            e.printStackTrace();
        }

        collector.ack(tuple);
        ttt = System.currentTimeMillis() - ttt;
        if (ttt > 500) {
            logger.warn("time to upsert status:" + ttt);
        }
    }

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        mapper = new ObjectMapper();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // declarer.declareStream("location", new Fields("vin", "message"));
    }

}
