package com.metazion.jm.net;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.AttributeKey;

public class TcpServer {

	private static final int BOSSTHREADNUMBER = 1;
	private static final int WORKERTHREADNUMBER = Runtime.getRuntime().availableProcessors();

	private final AttributeKey<ListenSession> LISTENSESSIONKEY = AttributeKey.valueOf("LISTENSESSIONKEY");
	private final AttributeKey<ServerSession> SERVERSESSIONKEY = AttributeKey.valueOf("SERVERSESSIONKEY");

	private final ServerBootstrap bootstrap = new ServerBootstrap();
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private ArrayList<ListenSession> listenSessions = new ArrayList<ListenSession>();

	public TcpServer() {

	}

	public void attach(ListenSession listenSession) {
		listenSessions.add(listenSession);
	}

	public void listen() {
		start();
		tryListenAll();
	}

	public void close() {
		stop();
	}

	private void start() {
		bossGroup = new NioEventLoopGroup(BOSSTHREADNUMBER);
		workerGroup = new NioEventLoopGroup(WORKERTHREADNUMBER);
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("encode", new ObjectEncoder());
				pipeline.addLast("decode", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
				pipeline.addLast(workerGroup, new ChannelInboundHandlerAdapter() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						ListenSession listenSession = ctx.channel().parent().attr(LISTENSESSIONKEY).get();
						ServerSession serverSession = listenSession.createServerSession();
						ctx.channel().attr(SERVERSESSIONKEY).set(serverSession);
						serverSession.setChannel(ctx.channel());
						serverSession.onActive();
					}

					@Override
					public void channelInactive(ChannelHandlerContext ctx) throws Exception {
						ServerSession serverSession = ctx.channel().attr(SERVERSESSIONKEY).get();
						serverSession.onInactive();
					}

					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						ServerSession serverSession = ctx.channel().attr(SERVERSESSIONKEY).get();
						serverSession.onException();
					}

					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						ServerSession serverSession = ctx.channel().attr(SERVERSESSIONKEY).get();
						serverSession.onReceive(msg);
					}
				});
			}
		});

		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
	}

	private void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
			bossGroup = null;
		}

		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}
	}

	private void tryListenAll() {
		for (ListenSession session : listenSessions) {
			tryListen(session);
		}
	}

	private void tryListen(ListenSession listenSession) {
		if (!listenSession.isWorking()) {
			return;
		}

		final int port = listenSession.getLocalPort();
		final int interval = listenSession.getRelistenInterval();

		ChannelFuture f = bootstrap.bind(port);
		f.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					f.channel().attr(LISTENSESSIONKEY).set(listenSession);
				} else {
					f.channel().eventLoop().schedule(() -> tryListen(listenSession), interval, TimeUnit.SECONDS);
				}
			}
		});
	}
}