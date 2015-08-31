package com.metazion.jm.net;

public abstract class ClientSession extends TransmitSession {

	private String remoteHost = "";
	private int remotePort = 0;
	private int reconnectInterval = 10;

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getReconnectInterval() {
		return reconnectInterval;
	}

	public void setReconnectInterval(int reconnectInterval) {
		this.reconnectInterval = reconnectInterval;
	}

	@Override
	public String detail() {
		return String.format("%s remoteHost[%s] remotePort[%d] reconnectInterval[%d]", super.detail(), remoteHost,
				remotePort, reconnectInterval);
	}
}