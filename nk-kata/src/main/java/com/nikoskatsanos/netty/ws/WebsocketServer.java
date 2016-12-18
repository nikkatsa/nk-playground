package com.nikoskatsanos.netty.ws;

import com.nikoskatsanos.jutils.core.threading.NamedThreadFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author nikkatsa
 */
public class WebsocketServer implements Callable<ChannelFuture> {

    private static final YalfLogger log = YalfLogger.getLogger(WebsocketServer.class);

    private final InetSocketAddress socketAddress;
    private final EventLoopGroup mainLoop;
    private final EventLoopGroup workerLoop;
    private final ChannelGroup channelGroup;
    private Channel channel;

    public WebsocketServer(int port) throws UnknownHostException {
        this(new InetSocketAddress(InetAddress.getLocalHost(), port));
    }

    public WebsocketServer(final InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        this.mainLoop = new NioEventLoopGroup(1, new NamedThreadFactory("Nio-Loop", true));
        this.workerLoop = new NioEventLoopGroup(50, new NamedThreadFactory("Worker-Loop", true));
        this.channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    }

    @Override
    public ChannelFuture call() throws Exception {
        log.info("Starting web socket server at: %s", this.socketAddress.toString());

        final ServerBootstrap server = new ServerBootstrap();
        server.group(this.mainLoop, this.workerLoop).channel(NioServerSocketChannel.class).childHandler(this
                .createChildHandlers());

        final ChannelFuture channelFuture = server.bind(this.socketAddress);
        channelFuture.syncUninterruptibly();
        this.channel = channelFuture.channel();

        return channelFuture;
    }

    private ChannelInitializer<Channel> createChildHandlers() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(final Channel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new ClientStatsHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                pipeline.addLast(new ClientConnectionHandler(channelGroup));
                pipeline.addLast(new ChatRoomHandler(channelGroup));
                pipeline.addLast(new FileStreamingHandler(channelGroup));
            }
        };
    }

    public static void main(final String... args) throws UnknownHostException {

        final Options options = new Options();
        options.addOption(Option.builder("p").longOpt("port").argName("port").desc("Port the web socket server " +
                "will be running on").hasArg(true).type(Integer.class).required(false).build());
        try {
            final CommandLine cmd = new DefaultParser().parse(options, args);

            final int port = cmd.hasOption('p') ? Integer.parseInt(cmd.getOptionValue('p')) : 9999;

            final WebsocketServer server = new WebsocketServer(port);
            final ChannelFuture channelFuture = Executors.newSingleThreadExecutor().submit(server).get();
            channelFuture.channel().closeFuture().syncUninterruptibly();

        } catch (final ParseException e) {
            log.error("", e);
        } catch (final InterruptedException | ExecutionException e) {
            log.error("", e);
        }
    }
}
