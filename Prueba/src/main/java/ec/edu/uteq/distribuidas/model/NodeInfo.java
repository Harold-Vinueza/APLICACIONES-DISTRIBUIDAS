package ec.edu.uteq.distribuidas.model;

public class NodeInfo {

    private final int nodeId;
    private final int port;

    public NodeInfo(int nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getPort() {
        return port;
    }
}