package example.storm.phoenix;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.Map;


public class PhoenixBolt extends BaseRichBolt {
    private OutputCollector collector;
    private ObjectMapper mapper;

    @Override
    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        mapper = new ObjectMapper();
    }

    @Override
    public void execute(Tuple input) {
        String sentence = input.getString(0);
//        System.out.print("*************************************************************"+sentence);


        JsonMessage msg = null;
        try {
            msg = mapper.readValue(sentence, JsonMessage.class);
            System.out.println("############################" + msg.getOrderID() + " " + msg.getAccount() + " " + msg.getVendorID() + " " + msg.getItemID() + " " + msg.getItemQuantity() + " " + msg.getStatus() + " " + msg.getTime());
        } catch (IOException e) {
            e.printStackTrace();
        }

        collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("kafkamessage"));
    }
}