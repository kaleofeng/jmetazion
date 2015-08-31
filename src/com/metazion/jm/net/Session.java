package com.metazion.jm.net;

import io.netty.channel.Channel;

public abstract class Session {

	protected Channel channel = null;

	public Session() {

	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public abstract void send(Object data);

	public abstract void onReceive(Object data) throws Exception;

	public abstract void onActive() throws Exception;

	public abstract void onInactive() throws Exception;

	public abstract void onException() throws Exception;
}