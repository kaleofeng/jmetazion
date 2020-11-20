package com.metazion.jm.net;

public abstract class ListenSession {

    private boolean working = false;

    private int localPort = 0;
    private int relistenInterval = 10;

    public void open() {
        working = true;
    }

    public void close() {
        working = false;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRelistenInterval() {
        return relistenInterval;
    }

    public void setRelistenInterval(int relistenInterval) {
        this.relistenInterval = relistenInterval;
    }

    public String detail() {
        return String.format("working[%b] localPort[%d] relistenInterval[%d]", working, localPort, relistenInterval);
    }

    public abstract ServerSession createServerSession();
}
