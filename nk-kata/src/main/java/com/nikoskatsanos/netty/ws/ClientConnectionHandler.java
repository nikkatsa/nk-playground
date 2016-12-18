package com.nikoskatsanos.netty.ws;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * @author nikkatsa
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final YalfLogger log = YalfLogger.getLogger(ClientConnectionHandler.class);

    private final ChannelGroup channelGroup;

    public ClientConnectionHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("Client connected: %s", ctx.channel().remoteAddress().toString());
            channelGroup.add(ctx.channel());
            channelGroup.writeAndFlush(new TextWebSocketFrame(String.format("%s joined", ctx.channel().remoteAddress
                    ().toString())));
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
        ReferenceCountUtil.retain(msg);
        ctx.fireChannelRead(msg);
    }
}
