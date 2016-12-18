package com.nikoskatsanos.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.time.LocalTime;
import java.util.Objects;

/**
 * @author nikkatsa
 */
public interface EchoMsg {

    String echo();

    ByteBuf toByteBuf();

    public static EchoMsg of(final String strMsg) {
        Objects.requireNonNull(strMsg, "Expected a message but null was found");

        if (strMsg.equalsIgnoreCase("exit")) {
            return new ExitEchoMessage();
        }

        return new TimestampEchoMessage(strMsg);
    }

    public abstract class SimpleEchoMessage implements EchoMsg {
        private final String msg;

        public SimpleEchoMessage(final String msg) {
            this.msg = msg;
        }

        @Override
        public String echo() {
            return this.msg;
        }

        @Override
        public ByteBuf toByteBuf() {
            return Unpooled.copiedBuffer(echo(), CharsetUtil.UTF_8);
        }

        @Override
        public String toString() {
            return String.format("%s { echo=%s }", getClass().toString(), echo());
        }
    }

    public class TimestampEchoMessage extends SimpleEchoMessage {

        public TimestampEchoMessage(final String msg) {
            super(msg);
        }

        @Override
        public String echo() {
            return String.format("[%s] %s", LocalTime.now().toString(), super.echo());
        }
    }

    public final class ExitEchoMessage extends TimestampEchoMessage {

        public ExitEchoMessage() {
            super("Bye");
        }

    }

}
