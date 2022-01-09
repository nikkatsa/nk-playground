package com.nikoskatsanos.netty.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContextBuilder;
import java.net.URI;
import java.security.KeyStore;
import java.util.Objects;
import javax.net.ssl.TrustManagerFactory;

public class NettyWSClient {

    public void start() {

        final EventLoopGroup bossLoop = new NioEventLoopGroup(1);
        Bootstrap client = new Bootstrap()
            .group(bossLoop)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();

                    KeyStore truststore = KeyStore.getInstance("JKS");
                    truststore.load(NettyWSClient.class.getResourceAsStream("/TestTruststore.jks"), "changeit".toCharArray());
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(truststore);

                    pipeline.addLast(SslContextBuilder.forClient().trustManager(trustManagerFactory).build().newHandler(channel.alloc()));

                    pipeline.addLast(new HttpClientCodec(512, 512, 512));
                    pipeline.addLast(new HttpObjectAggregator(16_384));
                    final String url = "wss://localhost:10000";
                    final WebSocketClientHandshaker13 wsHandshaker = new WebSocketClientHandshaker13(new URI(url),
                        WebSocketVersion.V13, "", false, new DefaultHttpHeaders(false), 64_000);
                    pipeline.addLast(new WebSocketClientProtocolHandler(wsHandshaker));

                    pipeline.addLast(new SimpleChannelInboundHandler<TextWebSocketFrame>() {

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
                                WebSocketClientProtocolHandler.ClientHandshakeStateEvent handshakeStateEvent = (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
                                switch (handshakeStateEvent) {
                                    case HANDSHAKE_COMPLETE:
                                        System.out.println("Handshake completed. Sending Hello World");
                                        ctx.writeAndFlush(new TextWebSocketFrame("Hello World"));
                                        break;
                                }
                            }
                        }

                        @Override
                        protected void channelRead0(final ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
                            System.out.println("Message=" + msg.text());
                        }
                    });
                }
            });
        client.connect("localhost", 10_000).channel().closeFuture().syncUninterruptibly();
        System.out.println("END");
    }

    public static void main(String[] args) {
        new NettyWSClient().start();
    }
}
