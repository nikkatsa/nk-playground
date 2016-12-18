package com.nikoskatsanos.netty.echo;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author nikkatsa
 */
public class EchoServer {

    private static final YalfLogger log = YalfLogger.getLogger(EchoServer.class);

    private final int port;

    public EchoServer() {
        this(8080);
    }

    public EchoServer(final int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        final NioEventLoopGroup mainLoop = new NioEventLoopGroup();

        try {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(mainLoop).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress
                    (port)).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new EchoServerHandler());
                }
            });

            final ChannelFuture channelFuture = serverBootstrap.bind().sync();
            log.info("Server started at %s", channelFuture.channel().localAddress().toString());
            channelFuture.channel().closeFuture().sync();
        } finally {
            mainLoop.shutdownGracefully().sync();
        }
    }

    @ChannelHandler.Sharable
    public class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
            final ByteBuf inboundMsg = (ByteBuf) msg;
            final String strMsg = inboundMsg.toString(CharsetUtil.UTF_8);

            final EchoMsg echoMsg = EchoMsg.of(strMsg);
            if (echoMsg instanceof EchoMsg.ExitEchoMessage) {
                log.info("Client [%s] exiting...", ctx.channel().remoteAddress().toString());

                ctx.write(echoMsg.toByteBuf());
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            log.info("<<[%s]: %s", ctx.channel().remoteAddress().toString(), echoMsg.echo());
            ctx.write(echoMsg.toByteBuf());
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
            log.error(cause.getMessage(), cause);
            ctx.close();
        }
    }

    public static void main(final String... args) {
        log.info("");
        try {
            new EchoServer().start();
        } catch (final InterruptedException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
