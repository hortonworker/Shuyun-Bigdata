package example.storm.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public enum PhoenixClient {
    INSTANCE;

    String URL;

    PhoenixClient() {
        String zookeepers = Configuration.getConfigValue("hbase.zookeeper.quorum");
        int port = Integer.parseInt(Configuration.getConfigValue("hbase.zookeeper.property.clientPort"));
        String root = Configuration.getConfigValue("zookeeper.znode.parent");
        URL = String.format("jdbc:phoenix:%s:%d:%s", zookeepers, port, root);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public Connection getConnection(long ts) throws SQLException {
        // FOR DEVELOP
        if (ts > System.currentTimeMillis()) {
            return getConnection();
        }

        Properties props = new Properties();
        props.setProperty("CurrentSCN", Long.toString(ts));
        return DriverManager.getConnection(URL, props);
    }
}
