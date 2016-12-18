package com.nikoskatsanos.netty.ws;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.EmptyHeaders;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class WSClient {

    private static final YalfLogger log = YalfLogger.getLogger(WSClient.class);


    public static void main(final String... args) throws UnknownHostException, InterruptedException,
            URISyntaxException {

        final EventLoopGroup mainLoop = new NioEventLoopGroup(1, new NamedThreadFactory("WS-Client", true));

        final WebSocketClientProtocolHandler wsProtocolHandler = new WebSocketClientProtocolHandler
                (WebSocketClientHandshakerFactory.newHandshaker(new URI("ws://192.168.0.5:9999"), WebSocketVersion
                        .V13, null, false, new DefaultHttpHeaders()));

        final TempChannelOut writer = new TempChannelOut();
        final Bootstrap clientBootStrap = new Bootstrap().group(mainLoop).channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(InetAddress.getLocalHost(), 9999)).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(final Channel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpClientCodec());
                pipeline.addLast(new HttpObjectAggregator(8192));
                pipeline.addLast(wsProtocolHandler);
                pipeline.addLast(new TempChannelHandler());
                pipeline.addLast(writer);
            }
        });
        final ChannelFuture channelFuture = clientBootStrap.connect().sync();
        final Channel channel = channelFuture.channel();

        while (true) {
            TimeUnit.MILLISECONDS.sleep(3000L);
            writer.sendMsg();
        }
    }

    private static class TempChannelHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
            log.info("<<: %s", msg.text());
        }
    }

    private static class TempChannelOut extends ChannelOutboundHandlerAdapter {

        private ChannelHandlerContext channelCtx;

        private volatile String localTimeStr;

        public TempChannelOut() {
            this.localTimeStr = LocalTime.now().toString();

            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                localTimeStr = LocalTime.now().toString();
            }, 9L, 500L, TimeUnit.MILLISECONDS);
        }

        @Override
        public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress
                localAddress, final ChannelPromise promise) throws Exception {
            super.connect(ctx, remoteAddress, localAddress, promise);
            this.channelCtx = ctx;
        }

        public void sendMsg() {
            this.channelCtx.writeAndFlush(new TextWebSocketFrame(localTimeStr));
        }

        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws
                Exception {
            super.write(ctx, msg, promise);
            log.info(">>: %s", msg);
        }
    }

}
