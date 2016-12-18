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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public class WebsocketClient implements Callable<ChannelFuture> {
    private static final YalfLogger log = YalfLogger.getLogger(WebsocketClient.class);

    private final int botId;
    private final InetSocketAddress serverAddress;
    private final URI wsURI;
    private final long publishRate;
    private final TimeUnit timeUnit;

    public WebsocketClient(int botId, InetSocketAddress serverAddress) {
        this(botId, serverAddress, 5000L, TimeUnit.MILLISECONDS);
    }

    public WebsocketClient(int botId, InetSocketAddress serverAddress, long publishRate, TimeUnit timeUnit) {
        this.botId = botId;
        this.serverAddress = serverAddress;
        this.wsURI = URI.create(String.format("ws://%s:%d", this.serverAddress.getHostName(), this.serverAddress
                .getPort()));
        this.publishRate = publishRate;
        this.timeUnit = timeUnit;
    }

    @Override
    public ChannelFuture call() throws Exception {
        final EventLoopGroup mainLoop = new NioEventLoopGroup(1, new NamedThreadFactory("Main-Loop", true));

        final Bootstrap client = new Bootstrap();

        client.group(mainLoop).remoteAddress(this.serverAddress).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(final Channel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpClientCodec());
                pipeline.addLast(new HttpObjectAggregator(64_000));
                pipeline.addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory.newHandshaker
                        (wsURI, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                pipeline.addLast(new WebsocketClientMsgPrinter());
                pipeline.addLast(new WebsocketClientWriter(publishRate, timeUnit));
            }
        });

        final ChannelFuture channelFuture = client.connect().syncUninterruptibly();
        return channelFuture;
    }

    private class WebsocketClientMsgPrinter extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
            log.info("<< %s", msg.text());
        }
    }

    private class WebsocketClientWriter extends ChannelOutboundHandlerAdapter {

        private ChannelHandlerContext ctx;
        private final ScheduledThreadPoolExecutor publisher;
        private final long publishRate;
        private final TimeUnit timeUnit;
        private ScheduledFuture<?> publishTask = null;

        private volatile String timeNow = null;

        public WebsocketClientWriter(long publishRate, TimeUnit timeUnit) {
            this.publisher = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new NamedThreadFactory
                    ("WS-Client-Publisher", true));
            this.publisher.setRemoveOnCancelPolicy(true);
            this.publishRate = publishRate;
            this.timeUnit = timeUnit;

            this.timeNow = LocalTime.now().toString();
            Executors.newScheduledThreadPool(1, new NamedThreadFactory("Time-Provider", true)).scheduleAtFixedRate(()
                    -> timeNow = LocalTime.now().toString(), 0L, 100L, TimeUnit.MILLISECONDS);
        }

        @Override
        public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress
                localAddress, final ChannelPromise promise) throws Exception {
            super.connect(ctx, remoteAddress, localAddress, promise);

            log.info("Connection established to %s", remoteAddress.toString());
            this.ctx = ctx;

            this.publishTask = this.publisher.scheduleAtFixedRate(() -> {
                ctx.writeAndFlush(new TextWebSocketFrame(String.format("Bot-%d %s", botId, timeNow)));
            }, 0L, this.publishRate, this.timeUnit);
        }

        @Override
        public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
            super.disconnect(ctx, promise);
            if (Objects.nonNull(this.publishTask)) {
                this.publishTask.cancel(true);
            }
        }
    }

    public static void main(final String... args) throws ExecutionException, InterruptedException {
        final String host = "nikoskatsanos.com";
        final int port = 9999;


        final Set<ChannelFuture> channelFutures = new HashSet<ChannelFuture>();
        for (int i = 0; i < 10; i++) {
            final long rate = (long) ((Math.random() + 0.5) * 5000);
            final WebsocketClient client = new WebsocketClient(i + 1, InetSocketAddress.createUnresolved(host, port),
                    rate, TimeUnit.MILLISECONDS);
            final ChannelFuture channelFuture = Executors.newSingleThreadExecutor().submit(client).get();

            channelFutures.add(channelFuture.channel().closeFuture());
        }

        channelFutures.stream().forEach(f -> f.syncUninterruptibly());
    }
}
