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
public class ChatRoomHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final YalfLogger log = YalfLogger.getLogger(ChatRoomHandler.class);

    private final ChannelGroup channelGroup;

    public ChatRoomHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
        ReferenceCountUtil.retain(msg);
        log.info("<< %s", msg.text());
        this.channelGroup.writeAndFlush(new TextWebSocketFrame(String.format("%s says \"%s\"", ctx.channel()
                .remoteAddress().toString(), msg.text())));
        ctx.fireChannelRead(msg);
    }
}
