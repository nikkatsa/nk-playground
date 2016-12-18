package com.nikoskatsanos.netty.ws;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

/**
 * @author nikkatsa
 */
public class FileStreamingHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final YalfLogger log = YalfLogger.getLogger(FileStreamingHandler.class);

    private final ChannelGroup channelGroup;

    public FileStreamingHandler(final ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
    }
}
