package ec.edu.uteq.distribuidas.model;

public class NodeStatus {

    private boolean active = true;
    private long lastHeartbeat = System.currentTimeMillis();

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }
}