package com.caiyi.nirvana.analyse.cassandra.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mario on 2016/10/12 0012.
 * Cassandra连接配置类
 */
public class CassandraPoolOptions {
    private int heartbeatIntervalSeconds;
    private int localCoreConnectNum;
    private int localMaxConnectNum;
    private int remoteCoreConnectNum;
    private int remoteMaxConnectNum;
    private int port;
    private List<String> contactPoints;

    public List<InetAddress> getContactPoints() {
        return new ArrayList<InetAddress>() {{
            for (String item : contactPoints){
                try {
                    add(InetAddress.getByName(item));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }};
    }

    public void setContactPoints(List<String> contactPoints) {
        this.contactPoints = contactPoints;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public int getLocalCoreConnectNum() {
        return localCoreConnectNum;
    }

    public void setLocalCoreConnectNum(int localCoreConnectNum) {
        this.localCoreConnectNum = localCoreConnectNum;
    }

    public int getLocalMaxConnectNum() {
        return localMaxConnectNum;
    }

    public void setLocalMaxConnectNum(int localMaxConnectNum) {
        this.localMaxConnectNum = localMaxConnectNum;
    }

    public int getRemoteCoreConnectNum() {
        return remoteCoreConnectNum;
    }

    public void setRemoteCoreConnectNum(int remoteCoreConnectNum) {
        this.remoteCoreConnectNum = remoteCoreConnectNum;
    }

    public int getRemoteMaxConnectNum() {
        return remoteMaxConnectNum;
    }

    public void setRemoteMaxConnectNum(int remoteMaxConnectNum) {
        this.remoteMaxConnectNum = remoteMaxConnectNum;
    }
}
