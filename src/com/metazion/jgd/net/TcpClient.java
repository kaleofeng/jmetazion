package com.metazion.jgd.net;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.AttributeKey;

public class TcpClient {

	private final AttributeKey<ClientSession> SESSIONKEY = AttributeKey.valueOf("SESSIONKEY");

	private final Bootstrap bootstrap = new Bootstrap();
	private EventLoopGroup workerGroup = null;

	private ArrayList<ClientSession> clientSessions = new ArrayList<ClientSession>();

	public TcpClient() {

	}

	public void attach(ClientSession clientSession) {
		clientSessions.add(clientSession);
	}

	public void connect() {
		start();
		tryConnectAll();
	}

	public void close() {
		stop();
	}

	private void start() {
		workerGroup = new NioEventLoopGroup();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("encode", new ObjectEncoder());
				pipeline.addLast("decode", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
				pipeline.addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						ClientSession clientSession = ctx.channel().attr(SESSIONKEY).get();
						clientSession.setChannel(ctx.channel());
						clientSession.onActive();
					}

					@Override
					public void channelInactive(ChannelHandlerContext ctx) throws Exception {
						ClientSession clientSession = ctx.channel().attr(SESSIONKEY).get();
						clientSession.onInactive();

						final int interval = clientSession.getReconnectInterval();
						ctx.channel().eventLoop().schedule(() -> tryConnect(clientSession), interval, TimeUnit.SECONDS);
					}

					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						ClientSession clientSession = ctx.channel().attr(SESSIONKEY).get();
						clientSession.onException();
					}

					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						ClientSession clientSession = ctx.channel().attr(SESSIONKEY).get();
						clientSession.onReceive(msg);
					}
				});
			}
		});

		bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
	}

	private void stop() {
		workerGroup.shutdownGracefully();
	}

	private void tryConnectAll() {
		for (ClientSession clientSession : clientSessions) {
			tryConnect(clientSession);
		}
	}

	private void tryConnect(ClientSession clientSession) {
		if (!clientSession.isWorking()) {
			return;
		}

		final String host = clientSession.getRemoteHost();
		final int port = clientSession.getRemotePort();
		final int interval = clientSession.getReconnectInterval();

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					System.out.println(String.format("Tcp client connect success: %s", clientSession.detail()));
					f.channel().attr(SESSIONKEY).set(clientSession);
				} else {
					System.out.println(String.format("Tcp client connect failed: %s", clientSession.detail()));
					f.channel().eventLoop().schedule(() -> tryConnect(clientSession), interval, TimeUnit.SECONDS);
				}
			}
		});
	}
}