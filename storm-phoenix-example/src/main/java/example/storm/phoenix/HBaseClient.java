package example.storm.phoenix;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;


public enum HBaseClient {

    INSTANCE;

    private org.apache.hadoop.conf.Configuration HB_CONF = HBaseConfiguration.create();
    private Connection connection;

    HBaseClient() {
        try {
            HB_CONF.addResource("/hbase-site.xml");
            HB_CONF.addResource("/core-site.xml");
            HB_CONF.set("hbase.zookeeper.quorum", Configuration.getConfigValue("hbase.zookeeper.quorum"));
            HB_CONF.setInt("hbase.zookeeper.property.clientPort",
                    Integer.parseInt(Configuration.getConfigValue("hbase.zookeeper.property.clientPort")));
            HB_CONF.set("zookeeper.znode.parent", Configuration.getConfigValue("zookeeper.znode.parent"));
            connection = ConnectionFactory.createConnection(HB_CONF);
        } catch (ZooKeeperConnectionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Table getTable(TableName table) throws IOException {
        return connection.getTable(table);
    }

    public void close() throws IOException {
        if (connection == null) {
            return;
        }
        connection.close();
    }


}
