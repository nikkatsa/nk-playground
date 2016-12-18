package com.nikoskatsanos.netty.ws;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.nikoskatsanos.nkjutils.synthetic.metrics.MetricsFactory;
import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * <p>{@link io.netty.channel.ChannelInboundHandler} storing some statistics for the connects that are established</p>
 *
 * @author nikkatsa
 */
public class ClientStatsHandler extends ChannelInboundHandlerAdapter {

    private static final YalfLogger log = YalfLogger.getLogger(ClientStatsHandler.class);

    private final Counter connectedClients;
    private final Meter requestsRate;

    public ClientStatsHandler() {
        this.connectedClients = MetricsFactory.createCounter("ConnectedClients");
        this.requestsRate = MetricsFactory.createMeter("RequestsRate");
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        this.connectedClients.inc();
        log.info("ConnectedClients: %d", this.connectedClients.getCount());

        super.channelRegistered(ctx);
    }


    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        this.connectedClients.dec();
        log.info("ConnectedClients: %d", this.connectedClients.getCount());

        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        this.requestsRate.mark();

        super.channelRead(ctx, msg);
    }
}
