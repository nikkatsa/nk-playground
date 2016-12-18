package com.nikoskatsanos.netty.echo;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author nikkatsa
 */
public abstract class EchoClient {
    private static final YalfLogger log = YalfLogger.getLogger(EchoClient.class);

    private final int port;
    protected final EchoClientHandler echoClientHandler;

    public EchoClient() {
        this(8080);
    }

    public EchoClient(final int port) {
        this.port = port;
        this.echoClientHandler = new EchoClientHandler();
    }

    public void start() throws InterruptedException {
        final EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(mainEventLoopGroup).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress
                    (this.port)).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(echoClientHandler);
                }
            });
            final ChannelFuture channelFuture = bootstrap.connect().sync();

            this.echoClientStarted();

            channelFuture.channel().closeFuture().sync();
        } finally {
            log.info("Client exiting...");
            mainEventLoopGroup.shutdownGracefully().sync();

            this.echoClientStopped();
        }
    }

    protected void echoClientStarted() {
    }

    protected void echoClientStopped() {
    }

    @ChannelHandler.Sharable
    public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private volatile ChannelHandlerContext channelCtx;

        public void send(final String msg) {
            log.info(">> %s", msg);

            this.channelCtx.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            this.channelCtx = ctx;
        }

        @Override
        public void channelRead0(final ChannelHandlerContext ctx, final ByteBuf in) {
            log.info("<<[%s]: %s", ctx.channel().remoteAddress().toString(), in.toString(CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            log.error(cause.getMessage(), cause);
            ctx.close();
        }
    }
}
