package com.nikoskatsanos.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class NettyWSServer {

    public void start() {

        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final NioEventLoopGroup worker = new NioEventLoopGroup(1);
        final ServerBootstrap wsServer = new ServerBootstrap()
            .group(bossGroup, worker)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(final Channel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();

                    pipeline.addLast(createSSLContext().newHandler(channel.alloc()));

                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new HttpObjectAggregator(64_000));
                    pipeline.addLast(new WebSocketServerProtocolHandler("/"));

                    pipeline.addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {

                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
                            System.out.println("Message=" + msg.text());
                            ctx.writeAndFlush(new TextWebSocketFrame(msg.text() + " back"));
                        }
                    });
                }
            });

        System.out.println("WS Server started");
        wsServer.bind(10_000)
            .channel().closeFuture().syncUninterruptibly();
    }

    private SslContext createSSLContext() throws Exception{
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(NettyWSServer.class.getResourceAsStream("/TestKeystore.jks"), "changeit".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return SslContextBuilder.forServer(keyManagerFactory).build();
    }

    public static void main(String[] args) {
        new NettyWSServer().start();
    }
}
