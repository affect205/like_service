package org.alexside.like.engine.db;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;

public class CqlConnector {
    private Cluster cluster;
    private Session session;

    public void connect(String node, Integer port, String keyspace) {
        PoolingOptions poolingOptions = new PoolingOptions();
        Cluster.Builder b = Cluster.builder()
                .addContactPoint(node)
                .withPoolingOptions(poolingOptions
                        .setCoreConnectionsPerHost(HostDistance.LOCAL,  4)
                        .setMaxConnectionsPerHost( HostDistance.LOCAL, 10)
                        .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                        .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000)
                        .setHeartbeatIntervalSeconds(120));
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();
        session = cluster.connect();
        createKeyspace(keyspace);
        createTable(keyspace, "likes");
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        if (session != null) session.close();
        if (cluster != null) cluster.close();
    }

    protected void createKeyspace(String keyspace) {
        StringBuilder sb = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                .append(keyspace)
                .append(" WITH replication = {")
                        .append("'class':'").append("SimpleStrategy")
                        .append("','replication_factor':").append("1")
                        .append("};");
        String query = sb.toString();
        session.execute(query);
    }

    protected void createTable(String keyspace, String table) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(keyspace + "." + table).append("(")
                .append("player_id varchar primary key, ")
                .append("total counter);");
        session.execute(sb.toString());
    }
}